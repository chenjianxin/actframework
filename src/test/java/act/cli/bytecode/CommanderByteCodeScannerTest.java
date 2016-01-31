package act.cli.bytecode;

import act.TestBase;
import act.app.AppByteCodeScanner;
import act.app.AppCodeScannerManager;
import act.app.TestingAppClassLoader;
import act.asm.Type;
import act.cli.CliDispatcher;
import act.cli.meta.*;
import act.controller.meta.HandlerMethodMetaInfo;
import act.handler.CliHandler;
import act.job.AppJobManager;
import act.util.AsmTypes;
import act.util.Files;
import act.util.PropertySpec;
import org.junit.Before;
import org.junit.Test;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.S;
import testapp.cli.InstanceWithReturnType;
import testapp.cli.StaticWithoutReturnType;

import java.io.File;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommanderByteCodeScannerTest extends TestBase {

    private CommanderClassMetaInfoManager infoSrc;
    private TestingAppClassLoader classLoader;
    private AppCodeScannerManager scannerManager;
    private AppJobManager jobManager;
    private CommanderByteCodeScanner scanner;
    private CliDispatcher dispatcher;
    private File base;

    @Before
    public void setup() throws Exception {
        super.setup();
        scanner = new CommanderByteCodeScanner();
        scannerManager = mock(AppCodeScannerManager.class);
        jobManager = mock(AppJobManager.class);
        classLoader = new TestingAppClassLoader(mockApp);
        dispatcher = new CliDispatcher(mockApp);
        when(mockApp.classLoader()).thenReturn(classLoader);
        infoSrc = classLoader.commanderClassMetaInfoManager();
        when(mockApp.classLoader()).thenReturn(classLoader);
        when(mockApp.cliDispatcher()).thenReturn(dispatcher);
        when(mockApp.scannerManager()).thenReturn(scannerManager);
        when(mockApp.jobManager()).thenReturn(jobManager);
        when(mockAppConfig.possibleControllerClass(anyString())).thenReturn(true);
        when(mockRouter.isActionMethod(anyString(), anyString())).thenReturn(false);
        C.List<AppByteCodeScanner> scanners = $.cast(C.listOf(scanner));
        when(scannerManager.byteCodeScanners()).thenReturn(scanners);
        scanner.setApp(mockApp);
        base = new File("./target/test-classes");
    }

    @Test
    public void staticWithoutReturnValue() {
        scan(StaticWithoutReturnType.class);
        CommanderClassMetaInfo classMetaInfo = infoSrc.commanderMetaInfo(StaticWithoutReturnType.class.getName());
        CommandMethodMetaInfo methodMetaInfo = classMetaInfo.command("foo.bar");
        eq("doIt", methodMetaInfo.methodName());
        eq(StaticWithoutReturnType.class.getName() + ".doIt", methodMetaInfo.fullName());
        eq("help", methodMetaInfo.helpMsg());
        yes(methodMetaInfo.isStatic());
        assertNull(methodMetaInfo.propertySpec());
        eq(AsmTypes.RETURN_VOID, methodMetaInfo.returnType());
        C.List<CommandParamMetaInfo> params = methodMetaInfo.params();
        eq(2, params.size());

        CommandParamMetaInfo op = params.get(0);
        eq("op1", op.name());
        ParamOptionAnnoInfo anno = op.optionInfo();
        eq("-o", anno.lead1());
        eq("--op1", anno.lead2());

        op = params.get(1);
        eq("num", op.name());
        anno = op.optionInfo();
        eq("-n", anno.lead1());
        eq("--number", anno.lead2());
    }

    @Test
    public void instanceWithReturnValue() {
        scan(InstanceWithReturnType.class);
        CliHandler handler = dispatcher.handler("user.list");
        assertNotNull(handler);

        CommanderClassMetaInfo classMetaInfo = infoSrc.commanderMetaInfo(InstanceWithReturnType.class.getName());
        CommandMethodMetaInfo methodMetaInfo = classMetaInfo.command("user.list");
        eq("getUserList", methodMetaInfo.methodName());
        eq(InstanceWithReturnType.class.getName() + ".getUserList", methodMetaInfo.fullName());

        no(methodMetaInfo.isStatic());

        PropertySpec.MetaInfo dataView = methodMetaInfo.propertySpec();
        ceq(C.list("fn", "ln"), dataView.outputFields());

        eq(Type.getType(List.class), methodMetaInfo.returnType());

        C.List<CommandParamMetaInfo> params = methodMetaInfo.params();
        eq(4, params.size());

        CommandParamMetaInfo op = params.get(0);
        eq("id", op.name());
        ParamOptionAnnoInfo anno = op.optionInfo();
        eq("-i", anno.lead1());
        eq("--id", anno.lead2());
        eq("group1", anno.group());
        assertNull(anno.defVal());

        op = params.get(1);
        eq("b", op.name());
        assertNull(op.optionInfo());

        op = params.get(2);
        eq("limit", op.name());
        anno = op.optionInfo();
        eq("-l", anno.lead1());
        eq("--limit", anno.lead2());
        assertNull(anno.group());
        eq("-1", anno.defVal());

        op = params.get(3);
        eq("l", op.name());
        assertNull(op.optionInfo());
    }

    private void scan(Class<?> c) {
        List<File> files = Files.filter(base, S.F.contains(c.getSimpleName()));
        for (File file : files) {
            classLoader.preloadClassFile(base, file);
        }
        classLoader.scan();
    }

    private CommanderClassMetaInfo commander(String className) {
        return infoSrc.commanderMetaInfo("testapp.cli." + className);
    }

    private CommanderClassMetaInfo commander(Class<?> c) {
        return infoSrc.commanderMetaInfo(c.getName());
    }

    private CommandMethodMetaInfo command(String commander, String command) {
        CommanderClassMetaInfo cinfo = commander(commander);
        return null == cinfo ? null : cinfo.command(command);
    }

    private CommandMethodMetaInfo command(Class<?> c, String action) {
        CommanderClassMetaInfo cinfo = commander(c);
        return null == cinfo ? null : cinfo.command(action);
    }

    private void assertNoParam(HandlerMethodMetaInfo action) {
        same(0, action.paramCount());
    }

    private static enum _F {
        ;
        static $.Predicate<String> SYS_CLASS_NAME = new $.Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.startsWith("java") || s.startsWith("org.osgl.");
            }
        };
        static $.Predicate<String> SAFE_CLASS = S.F.endsWith(".class").and(SYS_CLASS_NAME.negate());
    }
}