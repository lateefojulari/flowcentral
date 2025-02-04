/*
 * Copyright 2021-2022 FlowCentral Technologies Limited.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.flowcentraltech.flowcentral.system.business;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.flowcentraltech.flowcentral.common.business.AbstractFlowCentralService;
import com.flowcentraltech.flowcentral.common.business.FileAttachmentProvider;
import com.flowcentraltech.flowcentral.common.business.LicenseProvider;
import com.flowcentraltech.flowcentral.common.business.SpecialParamProvider;
import com.flowcentraltech.flowcentral.common.business.SystemParameterProvider;
import com.flowcentraltech.flowcentral.common.constants.FlowCentralContainerPropertyConstants;
import com.flowcentraltech.flowcentral.common.constants.LicenseFeatureCodeConstants;
import com.flowcentraltech.flowcentral.common.constants.LicenseStatus;
import com.flowcentraltech.flowcentral.common.constants.SystemSchedTaskConstants;
import com.flowcentraltech.flowcentral.common.data.Attachment;
import com.flowcentraltech.flowcentral.common.data.ParamValueDef;
import com.flowcentraltech.flowcentral.common.data.ParamValuesDef;
import com.flowcentraltech.flowcentral.common.entities.FileAttachmentQuery;
import com.flowcentraltech.flowcentral.common.util.CommonInputUtils;
import com.flowcentraltech.flowcentral.configuration.business.ConfigurationLoader;
import com.flowcentraltech.flowcentral.configuration.constants.SysParamType;
import com.flowcentraltech.flowcentral.configuration.data.ModuleInstall;
import com.flowcentraltech.flowcentral.configuration.xml.ModuleAppConfig;
import com.flowcentraltech.flowcentral.configuration.xml.ModuleConfig;
import com.flowcentraltech.flowcentral.configuration.xml.SysParamConfig;
import com.flowcentraltech.flowcentral.system.constants.SystemLoadLicenseTaskConstants;
import com.flowcentraltech.flowcentral.system.constants.SystemModuleErrorConstants;
import com.flowcentraltech.flowcentral.system.constants.SystemModuleNameConstants;
import com.flowcentraltech.flowcentral.system.constants.SystemModuleSysParamConstants;
import com.flowcentraltech.flowcentral.system.data.CredentialDef;
import com.flowcentraltech.flowcentral.system.data.LicenseDef;
import com.flowcentraltech.flowcentral.system.data.LicenseEntryDef;
import com.flowcentraltech.flowcentral.system.data.ScheduledTaskDef;
import com.flowcentraltech.flowcentral.system.entities.Credential;
import com.flowcentraltech.flowcentral.system.entities.CredentialQuery;
import com.flowcentraltech.flowcentral.system.entities.DownloadLog;
import com.flowcentraltech.flowcentral.system.entities.Module;
import com.flowcentraltech.flowcentral.system.entities.ModuleApp;
import com.flowcentraltech.flowcentral.system.entities.ModuleAppQuery;
import com.flowcentraltech.flowcentral.system.entities.ModuleQuery;
import com.flowcentraltech.flowcentral.system.entities.ScheduledTask;
import com.flowcentraltech.flowcentral.system.entities.ScheduledTaskHist;
import com.flowcentraltech.flowcentral.system.entities.ScheduledTaskQuery;
import com.flowcentraltech.flowcentral.system.entities.SystemParameter;
import com.flowcentraltech.flowcentral.system.entities.SystemParameterQuery;
import com.flowcentraltech.flowcentral.system.util.LicenseUtils;
import com.tcdng.unify.core.Setting;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.annotation.Component;
import com.tcdng.unify.core.annotation.Configurable;
import com.tcdng.unify.core.annotation.Parameter;
import com.tcdng.unify.core.annotation.Periodic;
import com.tcdng.unify.core.annotation.PeriodicType;
import com.tcdng.unify.core.annotation.Synchronized;
import com.tcdng.unify.core.annotation.Taskable;
import com.tcdng.unify.core.annotation.TransactionAttribute;
import com.tcdng.unify.core.annotation.Transactional;
import com.tcdng.unify.core.application.Feature;
import com.tcdng.unify.core.application.FeatureQuery;
import com.tcdng.unify.core.constant.FileAttachmentType;
import com.tcdng.unify.core.constant.FrequencyUnit;
import com.tcdng.unify.core.criterion.Update;
import com.tcdng.unify.core.data.FactoryMap;
import com.tcdng.unify.core.data.Listable;
import com.tcdng.unify.core.data.Period;
import com.tcdng.unify.core.database.dynamic.sql.DynamicSqlDataSourceManager;
import com.tcdng.unify.core.security.TwoWayStringCryptograph;
import com.tcdng.unify.core.task.TaskExecLimit;
import com.tcdng.unify.core.task.TaskManager;
import com.tcdng.unify.core.task.TaskMonitor;
import com.tcdng.unify.core.task.TaskStatus;
import com.tcdng.unify.core.task.TaskStatusLogger;
import com.tcdng.unify.core.util.CalendarUtils;
import com.tcdng.unify.core.util.DataUtils;
import com.tcdng.unify.core.util.IOUtils;
import com.tcdng.unify.core.util.StringUtils;

/**
 * Implementation of system module service.
 * 
 * @author FlowCentral Technologies Limited
 * @since 1.0
 */
@Transactional
@Component(SystemModuleNameConstants.SYSTEM_MODULE_SERVICE)
public class SystemModuleServiceImpl extends AbstractFlowCentralService
        implements SystemModuleService, LicenseProvider, SpecialParamProvider, SystemParameterProvider {

    private static final String LICENSE = "license";

    @Configurable
    private ConfigurationLoader configurationLoader;

    @Configurable
    private DynamicSqlDataSourceManager dataSourceManager;

    @Configurable
    private TaskManager taskManager;

    @Configurable("scheduledtaskstatuslogger")
    private TaskStatusLogger taskStatusLogger;

    @Configurable
    private FileAttachmentProvider fileAttachmentProvider;

    private final FactoryMap<Long, ScheduledTaskDef> scheduledTaskDefs;

    private final FactoryMap<String, CredentialDef> authDefFactoryMap;

    private final FactoryMap<String, LicenseDef> licenseDefFactoryMap;

    private final List<String> featureList = Collections.unmodifiableList(Arrays.asList(
            LicenseFeatureCodeConstants.APPLICATION_WORKSPACES, LicenseFeatureCodeConstants.APPLICATION_COLLABORATION,
            LicenseFeatureCodeConstants.APPLICATION_AUDIT, LicenseFeatureCodeConstants.APPLICATION_ARCHIVING));

    public SystemModuleServiceImpl() {
        this.authDefFactoryMap = new FactoryMap<String, CredentialDef>(true)
            {
                @Override
                protected boolean stale(String authName, CredentialDef credentialDef) throws Exception {
                    return (environment().value(long.class, "versionNo",
                            new CredentialQuery().name(authName)) > credentialDef.getVersionNo());
                }

                @Override
                protected CredentialDef create(String authName, Object... arg1) throws Exception {
                    Credential credential = environment().find(new CredentialQuery().name(authName));
                    if (credential == null) {
                        throw new UnifyException(SystemModuleErrorConstants.CANNOT_FIND_CREDENTIAL, authName);
                    }

                    return new CredentialDef(authName, credential.getUserName(), credential.getPassword(),
                            credential.getVersionNo());
                }
            };

        this.scheduledTaskDefs = new FactoryMap<Long, ScheduledTaskDef>(true)
            {

                @Override
                protected boolean stale(Long scheduledTaskId, ScheduledTaskDef scheduledTaskDef) throws Exception {
                    return environment().value(long.class, "versionNo",
                            new ScheduledTaskQuery().id(scheduledTaskId)) > scheduledTaskDef.getVersion();
                }

                @Override
                protected ScheduledTaskDef create(Long scheduledTaskId, Object... params) throws Exception {
                    ScheduledTask scheduledTask = environment().find(ScheduledTask.class, scheduledTaskId);

                    String lock = "scheduledtask-lock" + scheduledTaskId;
                    long startTimeOffset = CalendarUtils.getTimeOfDayOffset(scheduledTask.getStartTime());
                    long endTimeOffset = 0;
                    if (scheduledTask.getEndTime() != null) {
                        endTimeOffset = CalendarUtils.getTimeOfDayOffset(scheduledTask.getEndTime());
                    } else {
                        endTimeOffset = CalendarUtils.getTimeOfDayOffset(CalendarUtils.getLastSecondDate(getNow()));
                    }

                    long repeatMillSecs = 0;
                    Period period = DataUtils.convert(Period.class, scheduledTask.getFrequency());
                    if (period != null && period.getMagnitude() > 0 && period.getUnit() != null) {
                        repeatMillSecs = CalendarUtils.getMilliSecondsByFrequency(period.getUnit(),
                                period.getMagnitude());
                    }

                    ParamValuesDef pvd = CommonInputUtils.getParamValuesDef(
                            taskManager.getTaskParameters(scheduledTask.getTaskName()), scheduledTask.getParamValues());
                    String[] weekDays = DataUtils.convert(String[].class, scheduledTask.getWeekdays());
                    String[] days = DataUtils.convert(String[].class, scheduledTask.getDays());
                    String[] months = DataUtils.convert(String[].class, scheduledTask.getMonths());
                    return new ScheduledTaskDef(scheduledTaskId, lock, scheduledTask.getDescription(),
                            scheduledTask.getTaskName(), startTimeOffset, endTimeOffset, repeatMillSecs, weekDays, days,
                            months, pvd, scheduledTask.getVersionNo());
                }
            };

        this.licenseDefFactoryMap = new FactoryMap<String, LicenseDef>(true)
            {

                @Override
                protected boolean stale(String name, LicenseDef licenseDef) throws Exception {
                    return environment().value(long.class, "versionNo",
                            new FileAttachmentQuery().id(licenseDef.getId())) > licenseDef.getVersionNo();
                }

                @Override
                protected LicenseDef create(String name, Object... params) throws Exception {
                    Attachment attachment = ensureLicense();
                    return getLicenseDef(attachment);
                }

            };
    }

    public void setConfigurationLoader(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    public void setDataSourceManager(DynamicSqlDataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void setTaskStatusLogger(TaskStatusLogger taskStatusLogger) {
        this.taskStatusLogger = taskStatusLogger;
    }

    public void setFileAttachmentProvider(FileAttachmentProvider fileAttachmentProvider) {
        this.fileAttachmentProvider = fileAttachmentProvider;
    }

    @Override
    public Long createDownloadLog(DownloadLog downloadLog) throws UnifyException {
        return (Long) environment().create(downloadLog);
    }

    @Override
    public List<Credential> findCredentials(CredentialQuery query) throws UnifyException {
        return environment().listAll(query);
    }

    @Override
    public CredentialDef getCredentialDef(String credName) throws UnifyException {
        return authDefFactoryMap.get(credName);
    }

    @Override
    public Long createModule(Module module) throws UnifyException {
        return (Long) environment().create(module);
    }

    @Override
    public List<Module> findModules(ModuleQuery query) throws UnifyException {
        return environment().listAll(query);
    }

    @Override
    public Module findModule(String moduleName) throws UnifyException {
        return environment().find(new ModuleQuery().name(moduleName));
    }

    @Override
    public Long getModuleId(String moduleName) throws UnifyException {
        return environment().value(Long.class, "id", new ModuleQuery().name(moduleName));
    }

    @Override
    public String getModuleName(Long moduleId) throws UnifyException {
        return environment().value(String.class, "name", new ModuleQuery().id(moduleId));
    }

    @Override
    public List<String> getAllModuleNames() throws UnifyException {
        return environment().valueList(String.class, "name", new ModuleQuery().ignoreEmptyCriteria(true));
    }

    @Override
    public <T> T getSysParameterValue(Class<T> clazz, String code) throws UnifyException {
        return internalGetSysParameterValue(clazz, code);
    }

    @Override
    public int setSysParameterValue(String code, Object value) throws UnifyException {
        return environment().updateAll(new SystemParameterQuery().code(code),
                new Update().add("value", convert(String.class, value, null)));
    }

    @Override
    public List<? extends Listable> getNamesSystemParameters() throws UnifyException {
        return environment()
                .listAll(new SystemParameterQuery().type(SysParamType.NAME).addSelect("code", "description"));
    }

    @Override
    public List<? extends Listable> getContactSystemParameters() throws UnifyException {
        return environment()
                .listAll(new SystemParameterQuery().type(SysParamType.CONTACT).addSelect("code", "description"));
    }

    @Override
    public Object resolveSpecialParameter(String param) throws UnifyException {
        if (param != null && param.startsWith("{{") && param.endsWith("}}")) {
            String key = param.substring(2, param.length() - 2);
            if ("u:loginId".equals(key)) {
                return getUserToken().getUserLoginId();
            } else if (key.startsWith("s:")) {
                return getSessionAttribute(key.substring(2));
            } else if (key.startsWith("p:")) {
                return getSysParameterValue(String.class, key.substring(2));
            }
        }

        return param;
    }

    @Override
    public byte[] generateLicenseRequest(String clientTitle, String clientAccount, Date requestDt)
            throws UnifyException {
        Feature deploymentID = environment().find(new FeatureQuery().code("deploymentID"));
        Feature deploymentInitDate = environment().find(new FeatureQuery().code("deploymentInitDate"));
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        pw.println(clientTitle);
        pw.println(clientAccount);
        pw.println(requestDt.getTime());
        pw.println(deploymentID.getValue());
        pw.println(deploymentInitDate.getValue());
        LicenseDef licenseDef = licenseDefFactoryMap.get(LICENSE);
        for (LicenseEntryDef entry : licenseDef.getEntryList()) {
            pw.println(LicenseUtils.getLineFromLicenseEntry(entry));
        }

        pw.flush();

        TwoWayStringCryptograph cryptograph = (TwoWayStringCryptograph) getComponent("twoway-stringcryptograph");
        String request = cryptograph.encrypt(writer.toString());
        String[] lines = StringUtils.splitIntoLengths(request, 40);
        writer = new StringWriter();
        pw = new PrintWriter(writer);
        for (String line : lines) {
            pw.println(line);
        }

        pw.flush();
        return writer.toString().getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public LicenseDef getInstanceLicensing() throws UnifyException {
        return licenseDefFactoryMap.get(LICENSE);
    }

    @Override
    public boolean isLicensed(String featureCode) throws UnifyException {
        return licenseDefFactoryMap.get(LICENSE).isLicensed(featureCode);
    }

    @Override
    public Date licensedExpiresOn(String featureCode) throws UnifyException {
        return licenseDefFactoryMap.get(LICENSE).getLicenseEntryDef(featureCode).getExpiryDate();
    }

    @Override
    public LicenseStatus getLicenseStatus(String featureCode) throws UnifyException {
        return licenseDefFactoryMap.get(LICENSE).getLicenseEntryDef(featureCode).getStatus();
    }

    @Periodic(PeriodicType.EON)
    public void refreshLicense(TaskMonitor taskMonitor) throws UnifyException {
        licenseDefFactoryMap.clear();
    }

    @Synchronized("sys:ensurelicense")
    public Attachment ensureLicense() throws UnifyException {
        Attachment attachment = fileAttachmentProvider.retrieveFileAttachment(LICENSE, "system.credential", 0L,
                LICENSE);
        if (attachment == null) {
            Feature deploymentID = environment().find(new FeatureQuery().code("deploymentID"));
            Feature deploymentInitDate = environment().find(new FeatureQuery().code("deploymentInitDate"));
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            if ("Enterprise".equalsIgnoreCase(getContainerSetting(String.class,
                    FlowCentralContainerPropertyConstants.FLOWCENTRAL_INSTALLATION_TYPE))) {
                pw.println("Enterprise");
                pw.println("Client Title");
                Date issue = CalendarUtils.getMidnightDate(new Date(Long.valueOf(deploymentInitDate.getValue())));
                Date expiry = new Date(
                        CalendarUtils.getLastSecondDate(issue).getTime() + LicenseUtils.EVALUATION_PERIOD);
                for (String featureCode : featureList) {
                    pw.println(LicenseUtils.getLineFromLineItems(featureCode, issue, expiry, 0));
                }
            } else {
                pw.println("Standard");
                pw.println("Client Title");
            }

            pw.flush();

            TwoWayStringCryptograph cryptograph = (TwoWayStringCryptograph) getComponent("twoway-stringcryptograph",
                    new Setting("encryptionKey", deploymentID.getValue() + "." + deploymentInitDate.getValue()));
            String license = writer.toString();
            String encLicense = cryptograph.encrypt(license);

            final Attachment _attachment = Attachment.newBuilder(FileAttachmentType.TEXT).name(LICENSE).title(LICENSE)
                    .fileName(LICENSE).data(encLicense.getBytes()).build();
            fileAttachmentProvider.saveFileAttachment(LICENSE, "system.credential", 0L, _attachment);
            attachment = fileAttachmentProvider.retrieveFileAttachment(LICENSE, "system.credential", 0L, LICENSE);
        }

        return attachment;
    }

    @Taskable(name = SystemLoadLicenseTaskConstants.LOADLICENSE_TASK_NAME, description = "Load License Task",
            parameters = { @Parameter(name = SystemLoadLicenseTaskConstants.LOADLICENSE_UPLOAD_FILE,
                    description = "$m{system.loadlicense.form.selectfile}", type = byte[].class, mandatory = true) },
            limit = TaskExecLimit.ALLOW_MULTIPLE, schedulable = false)
    public int executeLoadLicenseTask(TaskMonitor taskMonitor, byte[] licenseFile) throws UnifyException {
        logDebug(taskMonitor, "Loading license file...");
        try {
            Feature deploymentID = environment().find(new FeatureQuery().code("deploymentID"));
            Feature deploymentInitDate = environment().find(new FeatureQuery().code("deploymentInitDate"));
            TwoWayStringCryptograph cryptograph = (TwoWayStringCryptograph) getComponent("twoway-stringcryptograph",
                    new Setting("encryptionKey", deploymentID.getValue() + "." + deploymentInitDate.getValue()));
            BufferedReader reader = new BufferedReader(new StringReader(new String(licenseFile, "UTF-8")));
            String license = cryptograph.decrypt(IOUtils.readAll(reader));
            reader = new BufferedReader(new StringReader(license));
            final String type = reader.readLine();
            final String clientTitle = reader.readLine();
            final String instID = reader.readLine();
            final String instInitDate = reader.readLine();
            if (!deploymentID.getValue().equals(instID) || !deploymentInitDate.getValue().equals(instInitDate)) {
                throw new IllegalArgumentException("Invalid target ID");
            }

            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            pw.println(type);
            pw.println(clientTitle);
            String line = null;
            while ((line = reader.readLine()) != null) {
                pw.println(line);
            }

            pw.flush();

            license = writer.toString();
            String encLicense = cryptograph.encrypt(license);
            final Attachment _attachment = Attachment.newBuilder(FileAttachmentType.TEXT).name(LICENSE).title(LICENSE)
                    .fileName(LICENSE).data(encLicense.getBytes()).build();
            fileAttachmentProvider.saveFileAttachment(LICENSE, "system.credential", 0L, _attachment);
            logDebug(taskMonitor, "...license file successfully loaded...");
        } catch (IOException e) {
            throwOperationErrorException(e);
        }
        return 0;
    }

    @Override
    public void releaseScheduledTask(Long scheduledTaskId, Long scheduledTaskHistId, TaskStatus completionTaskStatus,
            String errorMsg) throws UnifyException {
        // Release lock on scheduled task
        ScheduledTaskDef scheduledTaskDef = scheduledTaskDefs.get(scheduledTaskId);
        releaseClusterLock(scheduledTaskDef.getLock());

        // Update history
        ScheduledTaskHist scheduledTaskHist = environment().find(ScheduledTaskHist.class, scheduledTaskHistId);
        scheduledTaskHist.setFinishedOn(getNow());
        scheduledTaskHist.setTaskStatus(completionTaskStatus);
        scheduledTaskHist.setErrorMsg(errorMsg);
        environment().updateByIdVersion(scheduledTaskHist);
    }

    @Periodic(PeriodicType.NORMAL)
    @Transactional(TransactionAttribute.REQUIRES_NEW)
    public void triggerScheduledTasksForExecution(TaskMonitor taskMonitor) throws UnifyException {
        // If periodic task is canceled or scheduler is disabled cancel all
        // scheduled tasks
        if (taskMonitor.isCanceled() || !internalGetSysParameterValue(boolean.class,
                SystemModuleSysParamConstants.SYSTEM_SCHEDULER_ENABLED)) {
            return;
        }

        // Working dates
        final Date now = getNow();
        final Date workingDt = CalendarUtils.getMidnightDate(now);

        // Expiration allowance
        final int expirationAllowanceMins = internalGetSysParameterValue(int.class,

                SystemModuleSysParamConstants.SYSTEM_SCHEDULER_TRIGGER_EXPIRATION);
        final long expirationAllowanceMilliSec = CalendarUtils.getMilliSecondsByFrequency(FrequencyUnit.MINUTE,
                expirationAllowanceMins);

        final int maxScheduledTaskTrigger = internalGetSysParameterValue(int.class,

                SystemModuleSysParamConstants.SYSTEM_SCHEDULER_MAX_TRIGGER);

        // Fetch tasks ready to run
        logDebug("Fetching ready tasks...");
        List<Long> readyScheduledTaskIdList = db().valueList(Long.class, "id",
                new ScheduledTaskQuery().readyToRunOn(now));

        // Schedule tasks that are active only today
        logDebug("[{0}] potential scheduled task(s) to run...", readyScheduledTaskIdList.size());
        int triggered = 0;
        for (Long scheduledTaskId : readyScheduledTaskIdList) {
            ScheduledTaskDef scheduledTaskDef = scheduledTaskDefs.get(scheduledTaskId);
            String taskLock = scheduledTaskDef.getLock();
            logDebug("Attempting to grab scheduled task lock [{0}] ...", taskLock);

            if (!isWithClusterLock(taskLock) && grabClusterLock(taskLock)) {
                boolean lockHeldForTask = false;
                try {
                    logDebug("Grabbed scheduled task lock [{0}] ...", taskLock);
                    logDebug("Setting up scheduled task [{0}] ...", scheduledTaskDef.getDescription());
                    Map<String, Object> taskParameters = new HashMap<String, Object>();
                    taskParameters.put(SystemSchedTaskConstants.SCHEDULEDTASK_ID, scheduledTaskId);

                    Date nextExecutionOn = environment().value(Date.class, "nextExecutionOn",
                            new ScheduledTaskQuery().id(scheduledTaskId));
                    final Date startOn = CalendarUtils.getDateWithOffset(workingDt, scheduledTaskDef.getStartOffset());
                    Date expiryOn = CalendarUtils.getDateWithOffset(nextExecutionOn, expirationAllowanceMilliSec);
                    if (!now.before(startOn) && now.before(expiryOn)) {
                        // Task execution has not expired. Start task
                        // Load settings
                        for (ParamValueDef pvd : scheduledTaskDef.getParamValuesDef().getParamValueList()) {
                            taskParameters.put(pvd.getParamName(), pvd.getConvertedParamVal());
                        }

                        // Create history
                        ScheduledTaskHist scheduledTaskHist = new ScheduledTaskHist();
                        scheduledTaskHist.setScheduledTaskId(scheduledTaskId);
                        scheduledTaskHist.setStartedOn(now);
                        scheduledTaskHist.setTaskStatus(TaskStatus.INITIALIZED);
                        Long scheduledTaskHistId = (Long) environment().create(scheduledTaskHist);
                        taskParameters.put(SystemSchedTaskConstants.SCHEDULEDTASKHIST_ID, scheduledTaskHistId);

                        // Fire task
                        taskManager.startTask(scheduledTaskDef.getTaskName(), taskParameters, true,
                                taskStatusLogger.getName());
                        logDebug("Task [{0}] is setup to run...", scheduledTaskDef.getDescription());

                        lockHeldForTask = true;
                        triggered++;
                    }

                    // Calculate and set next execution
                    Date calcNextExecutionOn = null;
                    long repeatMillSecs = scheduledTaskDef.getRepeatMillSecs();
                    if (repeatMillSecs > 0) {
                        Date limit = CalendarUtils.getDateWithOffset(workingDt, scheduledTaskDef.getEndOffset());
                        long factor = ((now.getTime() - nextExecutionOn.getTime()) / repeatMillSecs) + 1;
                        long actNextOffsetMillSecs = factor * repeatMillSecs;
                        calcNextExecutionOn = CalendarUtils.getDateWithOffset(nextExecutionOn, actNextOffsetMillSecs);
                        if (calcNextExecutionOn.before(startOn) || calcNextExecutionOn.after(limit)) {
                            calcNextExecutionOn = null;
                        }
                    }

                    if (calcNextExecutionOn == null) {
                        if (now.before(startOn) && CalendarUtils.isWithinCalendar(scheduledTaskDef.getWeekdays(),
                                scheduledTaskDef.getDays(), scheduledTaskDef.getMonths(), startOn)) {
                            // Today start time
                            calcNextExecutionOn = startOn;
                        } else {
                            // Use next eligible date start time
                            calcNextExecutionOn = CalendarUtils.getDateWithOffset(
                                    CalendarUtils.getNextEligibleDate(scheduledTaskDef.getWeekdays(),
                                            scheduledTaskDef.getDays(), scheduledTaskDef.getMonths(), workingDt),
                                    scheduledTaskDef.getStartOffset());
                        }
                    }

                    environment().updateById(ScheduledTask.class, scheduledTaskId,
                            new Update().add("nextExecutionOn", calcNextExecutionOn).add("lastExecutionOn", now));
                    logDebug("Task [{0}] is scheduled to run next on [{1,date,dd/MM/yy HH:mm:ss}]...",
                            scheduledTaskDef.getDescription(), calcNextExecutionOn);

                } catch (UnifyException e) {
                    try {
                        releaseClusterLock(taskLock);
                    } catch (Exception e1) {
                    }
                } finally {
                    if (!lockHeldForTask) {
                        releaseClusterLock(taskLock);
                    }
                }
            }

            if (triggered >= maxScheduledTaskTrigger) {
                break;
            }
        }
    }

    @Override
    protected void doInstallModuleFeatures(final ModuleInstall moduleInstall) throws UnifyException {
        installModuleAndSystemParameters(moduleInstall);
    }

    private LicenseDef getLicenseDef(Attachment attachment) throws UnifyException {
        Set<String> licensed = new HashSet<String>();
        Date now = getNow();
        Feature deploymentID = environment().find(new FeatureQuery().code("deploymentID"));
        Feature deploymentInitDate = environment().find(new FeatureQuery().code("deploymentInitDate"));
        TwoWayStringCryptograph cryptograph = (TwoWayStringCryptograph) getComponent("twoway-stringcryptograph",
                new Setting("encryptionKey", deploymentID.getValue() + "." + deploymentInitDate.getValue()));
        String license = cryptograph.decrypt(new String(attachment.getData()));
        
        try (BufferedReader reader = new BufferedReader(new StringReader(license));) {
            String type = reader.readLine();
            LicenseDef.Builder ldb = LicenseDef.newBuilder(attachment.getId(), type, attachment.getVersionNo());

            String clientTitle = reader.readLine();
            ldb.clientTitle(clientTitle);
            if ("Enterprise".equalsIgnoreCase(type)) {
                setSysParameterValue(SystemModuleSysParamConstants.CLIENT_TITLE, clientTitle);
            }

            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split(",");
                String code = items[0];
                String desc = getApplicationMessage(code);
                ldb.addEntry(LicenseUtils.getLicenseEntryDefFromLineItems(items, desc, now));
                licensed.add(code);
            }

            for (String featureCode : featureList) {
                if (!licensed.contains(featureCode)) {
                    String desc = getApplicationMessage(featureCode);
                    ldb.addEntry(featureCode, desc, null, null, LicenseStatus.NOT_LICENSED, 0);
                }
            }

            return ldb.build();
        } catch (IOException e) {
            throwOperationErrorException(e);
        }

        return null;
    }

    private <T> T internalGetSysParameterValue(Class<T> clazz, String code) throws UnifyException {
        SystemParameter sysParameter = environment().find(new SystemParameterQuery().code(code));
        if (sysParameter == null) {
            throw new UnifyException(SystemModuleErrorConstants.SYSPARAM_WITH_CODE_UNKNOWN, code);
        }
        return convert(clazz, sysParameter.getValue(), null);
    }

    private void installModuleAndSystemParameters(final ModuleInstall moduleInstall) throws UnifyException {
        ModuleConfig moduleConfig = moduleInstall.getModuleConfig();
        String moduleDescription = resolveApplicationMessage(moduleConfig.getDescription());
        final String moduleName = moduleConfig.getName();
        Long moduleId = null;
        if (moduleConfig.isPrincipal()) {
            logDebug("Installing module principal definition [{0}]...", moduleDescription);
            Module existModule = environment().find(new ModuleQuery().name(moduleName));
            if (existModule == null) {
                Module module = new Module();
                module.setName(moduleName);
                module.setDescription(moduleDescription);
                module.setLabel(resolveApplicationMessage(moduleConfig.getLabel()));
                module.setShortCode(moduleConfig.getShortCode());
                moduleId = (Long) environment().create(module);
            } else {
                existModule.setDescription(moduleDescription);
                existModule.setLabel(resolveApplicationMessage(moduleConfig.getLabel()));
                existModule.setShortCode(moduleConfig.getShortCode());
                environment().updateByIdVersion(existModule);
                moduleId = existModule.getId();
            }
        } else {
            logDebug("Installing module extension definition [{0}]...", moduleDescription);
            moduleId = environment().value(Long.class, "id", new ModuleQuery().name(moduleName));
        }

        ModuleApp moduleApp = new ModuleApp();
        moduleApp.setModuleId(moduleId);
        if (moduleConfig.getModuleAppsConfig() != null
                && !DataUtils.isBlank(moduleConfig.getModuleAppsConfig().getModuleAppList())) {
            logDebug("Registering applications for module [{0}]...", moduleConfig.getName());
            for (ModuleAppConfig moduleAppConfig : moduleConfig.getModuleAppsConfig().getModuleAppList()) {
                ModuleApp oldModuleApp = environment().find(new ModuleAppQuery().name(moduleAppConfig.getName()));
                String shortDescription = resolveApplicationMessage(moduleAppConfig.getShortDescription());
                String longDescription = resolveApplicationMessage(moduleAppConfig.getLongDescription());
                if (oldModuleApp == null) {
                    moduleApp.setName(moduleAppConfig.getName());
                    moduleApp.setShortDescription(shortDescription);
                    moduleApp.setLongDescription(longDescription);
                    moduleApp.setConfigFile(moduleAppConfig.getConfigFile());
                    moduleApp.setAutoInstall(moduleAppConfig.isAutoInstall());
                    environment().create(moduleApp);
                } else {
                    oldModuleApp.setShortDescription(shortDescription);
                    oldModuleApp.setLongDescription(longDescription);
                    oldModuleApp.setConfigFile(moduleAppConfig.getConfigFile());
                    oldModuleApp.setAutoInstall(moduleAppConfig.isAutoInstall());
                    environment().updateByIdVersion(oldModuleApp);
                }
            }
        }

        logDebug("Installing system parameters for module [{0}]...", moduleConfig.getDescription());
        SystemParameter sysParameter = new SystemParameter();
        sysParameter.setModuleId(moduleId);
        if (moduleConfig.getSysParamsConfig() != null
                && !DataUtils.isBlank(moduleConfig.getSysParamsConfig().getSysParamList())) {
            logDebug("Installing system parameters for module [{0}]...", moduleConfig.getName());
            for (SysParamConfig sysParamConfig : moduleConfig.getSysParamsConfig().getSysParamList()) {
                SystemParameter oldSysParameter = environment()
                        .find(new SystemParameterQuery().code(sysParamConfig.getCode()));
                if (oldSysParameter == null) {
                    sysParameter.setCode(sysParamConfig.getCode());
                    sysParameter.setDescription(resolveApplicationMessage(sysParamConfig.getDescription()));
                    sysParameter.setValue(sysParamConfig.getDefaultVal());
                    sysParameter.setDefaultValue(sysParamConfig.getDefaultVal());
                    sysParameter.setEditor(sysParamConfig.getEditor());
                    sysParameter.setType(sysParamConfig.getType());
                    sysParameter.setControl(sysParamConfig.isControl());
                    sysParameter.setEditable(sysParamConfig.isEditable());
                    environment().create(sysParameter);
                } else {
                    oldSysParameter.setDescription(resolveApplicationMessage(sysParamConfig.getDescription()));
                    oldSysParameter.setDefaultValue(sysParamConfig.getDefaultVal());
                    oldSysParameter.setEditor(sysParamConfig.getEditor());
                    oldSysParameter.setType(sysParamConfig.getType());
                    oldSysParameter.setControl(sysParamConfig.isControl());
                    oldSysParameter.setEditable(sysParamConfig.isEditable());
                    environment().updateByIdVersion(oldSysParameter);
                }
            }
        }
    }

}
