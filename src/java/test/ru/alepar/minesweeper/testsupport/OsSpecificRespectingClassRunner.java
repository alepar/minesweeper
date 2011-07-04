package ru.alepar.minesweeper.testsupport;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class OsSpecificRespectingClassRunner extends BlockJUnit4ClassRunner {

    public OsSpecificRespectingClassRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        return excludeOsDependentTests(super.computeTestMethods());
    }

    private List<FrameworkMethod> excludeOsDependentTests(List<FrameworkMethod> methods) {
        List<FrameworkMethod> result = new ArrayList<FrameworkMethod>(methods.size());

        for (FrameworkMethod method : methods) {
            DesignedFor designedFor = method.getAnnotation(DesignedFor.class);
            if(designedFor == null || designedFor.value() == currentOs()) {
                result.add(method);
            } else {
                System.err.println("ignoring test due to os dependency: " + method.getMethod().toString());
            }
        }

        return result;
    }

    private static OS currentOs() {
        String osName = System.getProperty("os.name");
        if("Linux".equals(osName)) {
            return OS.LINUX;
        }
        if(osName.startsWith("Windows")) {
            return OS.WINDOWS;
        }

        throw new RuntimeException("couldnot detect operating system, os.name=" + osName);
    }
}
