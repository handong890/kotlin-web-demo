/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.webdemo.servlet;

import org.apache.naming.NamingContext;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.examples.ExamplesFolder;
import org.jetbrains.webdemo.examples.ExamplesLoader;
import org.jetbrains.webdemo.handlers.ServerHandler;
import org.jetbrains.webdemo.help.HelpLoader;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KotlinHttpServlet extends HttpServlet {

    private final ServerHandler myHandler = new ServerHandler();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.setProperty("kotlin.running.in.server.mode", "true");
        System.setProperty("java.awt.headless", "true");

        ApplicationSettings.WEBAPP_ROOT_DIRECTORY = getServletContext().getRealPath("/");
        ApplicationSettings.EXAMPLES_DIRECTORY = ApplicationSettings.WEBAPP_ROOT_DIRECTORY + "examples";
        CommonSettings.HELP_DIRECTORY = ApplicationSettings.WEBAPP_ROOT_DIRECTORY;

        if (!loadTomcatParameters()) {
            ErrorWriter.writeErrorToConsole("FATAL ERROR: Cannot load parameters from tomcat config, server didn't start");
            System.exit(1);
        }

        ErrorWriter.ERROR_WRITER = ErrorWriter.getInstance();
//        Initializer.INITIALIZER = ServerInitializer.getInstance();

        try {
            ErrorWriter.writeInfoToConsole("Use \"help\" to look at all options");
            new File(CommonSettings.LOGS_DIRECTORY).mkdirs();
            LogWriter.init();
            ExamplesLoader.loadAllExamples();
            HelpLoader.getInstance();
            MySqlConnector.getInstance();

            List<String> tasksIdentifiers = new ArrayList<>();
            for(ExamplesFolder folder : ExamplesFolder.ROOT_FOLDER.getChildFolder("Workshop").getChildFolders()){
                for(Project example : folder.getExamples()){
                    tasksIdentifiers.add(example.id);
                }
            }
            MySqlConnector.getInstance().createTaskList(tasksIdentifiers);
        } catch (Throwable e) {
            ErrorWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
            System.exit(1);
        }
    }

    private boolean isWindows() {
        return (System.getProperty("os.name").toLowerCase().contains("win"));
    }

    private boolean loadTomcatParameters() {
        InitialContext initCtx = null;
        try {
            initCtx = new InitialContext();
            NamingContext envCtx = (NamingContext) initCtx.lookup("java:comp/env");
            try {
                CommandRunner.setServerSettingFromTomcatConfig("app_output_dir", (String) envCtx.lookup("app_output_dir"));
            } catch (NamingException e) {
                File rootFolder = new File(ApplicationSettings.WEBAPP_ROOT_DIRECTORY);
                String appHome = rootFolder.getParentFile().getParentFile().getParent();
                CommandRunner.setServerSettingFromTomcatConfig("app_output_dir", appHome);
            }

            try {
                CommandRunner.setServerSettingFromTomcatConfig("is_test_version", (String) envCtx.lookup("is_test_version"));
            } catch (NameNotFoundException e) {
                //Absent is_test_version variable in context.xml
                CommandRunner.setServerSettingFromTomcatConfig("is_test_version", "false");
            }

            CommandRunner.setServerSettingFromTomcatConfig("backend_url", (String) envCtx.lookup("backend_url"));
            return true;
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }

    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        myHandler.handle(request, response);
    }

    @Override
    public void destroy() {
        getServletContext().log("destroy() called");
    }

}
