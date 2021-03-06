/*
 * MIT License
 *
 * Copyright (c) 2020 Ben Davies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package uk.co.bjdavies;

import com.google.inject.Guice;
import com.google.inject.Injector;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import lombok.extern.log4j.Log4j2;
import uk.co.bjdavies.api.IApplication;
import uk.co.bjdavies.api.command.ICommandDispatcher;
import uk.co.bjdavies.api.config.IConfig;
import uk.co.bjdavies.api.discord.IDiscordFacade;
import uk.co.bjdavies.api.plugins.IPluginContainer;
import uk.co.bjdavies.api.variables.IVariableContainer;
import uk.co.bjdavies.command.CommandDispatcher;
import uk.co.bjdavies.command.CommandModule;
import uk.co.bjdavies.config.ConfigModule;
import uk.co.bjdavies.core.CorePlugin;
import uk.co.bjdavies.db.DB;
import uk.co.bjdavies.discord.DiscordModule;
import uk.co.bjdavies.http.WebServer;
import uk.co.bjdavies.plugins.PluginModule;
import uk.co.bjdavies.plugins.importing.ImportPluginFactory;
import uk.co.bjdavies.variables.VariableContainer;
import uk.co.bjdavies.variables.VariableModule;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Main Application Class for BabbleBot where all the services and providers get executed.
 * <p>
 * When developing plugins be sure to extend this app and override loadPlugins and include your plugin for testing.
 * Please see github wiki for more information.
 * </p>
 *
 * @author ben.davies99@outlook.com (Ben Davies)
 * @since 1.0.0
 */

@Log4j2
public class Application implements IApplication {

    private static int triedToMake = 0;
    private final Injector applicationInjector;
    private final IConfig config;
    private final ICommandDispatcher commandDispatcher;
    private final IVariableContainer variableContainer;
    private final IPluginContainer pluginContainer;
    private String serverVersion;
    private final WebServer webServer;
    private final Class<?> mainClass;
    private final String[] args;

    /**
     * Create a {@link Application} for BabbleBot
     *
     * @param args      this is the arguments passed in the by the cli.
     * @param mainClass - this is the class where the main method is
     */
    private Application(String[] args, Class<?> mainClass) {
        this.mainClass = mainClass;
        this.args = args;
        log.info("Started Application");
        log.info("Args: " + Arrays.toString(args));
        try {
            serverVersion = new String(getClass().getResourceAsStream("/version.txt").readAllBytes()).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ApplicationModule applicationModule = new ApplicationModule(this);
        ConfigModule configModule = new ConfigModule("config.json"); //TODO: use commandLine Arguments to fulfill this. e.g. -bconf.configLocation=config.json
        CommandModule commandModule = new CommandModule();
        VariableModule variableModule = new VariableModule();
        PluginModule pluginModule = new PluginModule(this);


        commandDispatcher = commandModule.getCommandDispatcher();
        variableContainer = variableModule.getContainer();
        pluginContainer = pluginModule.getPluginContainer();

        config = configModule.getConfig();
        DB.install(config.getDatabaseConfig());

        //Important Order Needs Config!!...
        DiscordModule discordModule = new DiscordModule(this);


        applicationInjector = Guice.createInjector(applicationModule, configModule, discordModule, commandModule,
                variableModule, pluginModule);

        pluginContainer.addPlugin("core", get(CorePlugin.class));

        config.getPlugins().forEach(pluginConfig -> ImportPluginFactory.importPlugin(pluginConfig, this)
                .subscribe(pluginContainer::addPlugin));

        discordModule.startDiscordBot();

        webServer = get(WebServer.class);
        webServer.start();

    }

    /**
     * This will make a Application and ensure that only one application can be made.
     *
     * @param args The Arguments passed in by the cli.
     * @return {@link IApplication}
     * @throws RuntimeException if the application tried to made twice
     */
    public static IApplication make(Class<?> mainClass, String[] args) {
        return make(mainClass, Application.class, args);
    }

    /**
     * This will make a Application and ensure that only one application can be made.
     * 2.0.0
     *
     * @param args  The Arguments passed in by the cli.
     * @param clazz Bootstrap your own application useful for developing plugins.
     * @return {@link IApplication}
     * @throws RuntimeException if the application tried to made twice
     */
    public static <T extends Application> IApplication make(Class<?> mainClass, Class<T> clazz, String[] args) {
        if (triedToMake == 0) {
            triedToMake++;
            try {
                return clazz.getDeclaredConstructor(String[].class, Class.class).newInstance(args, mainClass);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                log.error("Tried to bootstrap an application without having String[] args in the constructor or having a private constructor, Exiting....", e);
                System.exit(-1);
            }
        } else {
            log.error("Tried to create Multiple applications, Exiting...", new RuntimeException("Tried to make multiple Applications"));
            System.exit(-1);
            return null;
        }

        return null;
    }

    /**
     * This will return of a instance of a class and inject any dependencies that are inside the dependency injector.
     *
     * @param clazz - this is the class to create
     * @return Object<T> this is an object of type T
     */
    public <T> T get(Class<T> clazz) {
        return applicationInjector.getInstance(clazz);
    }

    /**
     * This will return an instance of the application config from config.json file in your root directory.
     *
     * @return {@link IConfig}
     */
    public IConfig getConfig() {
        return this.config;
    }

    /**
     * This will return an instance of the application command dispatcher.
     *
     * @return {@link CommandDispatcher}
     */
    @Override
    public ICommandDispatcher getCommandDispatcher() {
        return commandDispatcher;
    }

    /**
     * This will return an instance of the application variable container
     *
     * @return {@link VariableContainer}
     */
    @Override
    public IVariableContainer getVariableContainer() {
        return variableContainer;
    }

    @Override
    public IPluginContainer getPluginContainer() {
        return pluginContainer;
    }

    @Override
    public String getServerVersion() {
        return serverVersion;
    }

    @Override
    public void shutdown(int timeout) {
        Executors.newSingleThreadExecutor().submit(() -> {
            Timer timer = new Timer();

            getPluginContainer().shutDownPlugins();
            IDiscordFacade facade = get(IDiscordFacade.class);
            facade.logoutBot().block();
            webServer.stop();

            Runtime.getRuntime().runFinalization();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, timeout);
        });
    }

    @Override
    public boolean hasArgument(String argument) {
        return Arrays.asList(args).contains(argument);
    }

    @Override
    public void restart() {
        Executors.newSingleThreadExecutor().submit(() -> {
            getPluginContainer().shutDownPlugins();
            IDiscordFacade facade = get(IDiscordFacade.class);
            facade.registerEventHandler(DisconnectEvent.class, (d) -> {
                log.info("Bot has been logged out!!!");
            });
            facade.logoutBot().block();
            webServer.stop();
            Runtime.getRuntime().runFinalization();
            try {
                List<String> command = new ArrayList<>();

                if (new File("Babblebot").exists()) {
                    command.add("./Babblebot");
                } else {
                    String cmd = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                    command.add(cmd);
                    command.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
                    command.add("-cp");
                    command.add(ManagementFactory.getRuntimeMXBean().getClassPath());
                    command.add(mainClass.getName());
                    command.addAll(Arrays.asList(args));
                }
                command.add("-restart");


                ProcessBuilder pb = new ProcessBuilder();
                log.info("Built command: {} ", command);
                pb.command(command);
                pb.inheritIO();
                Process p = pb.start();
                p.waitFor();
                System.exit(p.exitValue());
                //System.exit(0);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
