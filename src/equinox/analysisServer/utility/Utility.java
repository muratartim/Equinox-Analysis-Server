package equinox.analysisServer.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.RandomUtils;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import equinox.analysisServer.client.ClientConnection;
import equinox.analysisServer.client.ClientHandler;
import equinox.analysisServer.remote.Registry;
import equinox.analysisServer.server.AnalysisServer;
import equinox.serverUtilities.FilerConnection;

/*
 * Copyright 2018 Murat Artim (muratartim@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Class for utility methods.
 *
 * @author Murat Artim
 * @date 15 Jun 2018
 * @time 17:49:41
 */
public class Utility {

	/** Buffer size for extracting zipped files. */
	private static final int BUFSIZE = 2048;

	/** OFF Server property encryptor. */
	// private static final StandardPBEStringEncryptor PROPERTY_ENCRYPTOR;
	// static {
	// PROPERTY_ENCRYPTOR = new StandardPBEStringEncryptor();
	// PROPERTY_ENCRYPTOR.setPassword("EquinoxAnalysisServer_2018");
	// }

	/**
	 * Creates logger for the server.
	 *
	 * @param logFileName
	 *            Log file name.
	 * @param logLevel
	 *            Log level.
	 * @return The newly created logger.
	 * @throws Exception
	 *             If logger cannot be created.
	 */
	public static Logger setupLogger(String logFileName, String logLevel) throws Exception {

		// create logger
		Logger logger = Logger.getLogger(AnalysisServer.class.getName());

		// create file handler
		FileHandler fileHandler = new FileHandler(logFileName);

		// set simple formatter to file handler
		fileHandler.setFormatter(new SimpleFormatter());

		// add handlers to logger
		logger.addHandler(fileHandler);

		// set log level
		if (logLevel.equals("all")) {
			logger.setLevel(Level.ALL);
		}
		else if (logLevel.equals("config")) {
			logger.setLevel(Level.CONFIG);
		}
		else if (logLevel.equals("fine")) {
			logger.setLevel(Level.FINE);
		}
		else if (logLevel.equals("finer")) {
			logger.setLevel(Level.FINER);
		}
		else if (logLevel.equals("finest")) {
			logger.setLevel(Level.FINEST);
		}
		else if (logLevel.equals("info")) {
			logger.setLevel(Level.INFO);
		}
		else if (logLevel.equals("off")) {
			logger.setLevel(Level.OFF);
		}
		else if (logLevel.equals("severe")) {
			logger.setLevel(Level.SEVERE);
		}
		else if (logLevel.equals("warning")) {
			logger.setLevel(Level.WARNING);
		}

		// log info
		logger.info("Server logger setup for " + logger.getLevel().getName() + " level logging.");

		// return logger
		return logger;
	}

	/**
	 * Creates and returns the network server.
	 *
	 * @param server
	 *            Server instance.
	 * @return The network server.
	 */
	public static Server setupNetworkServer(AnalysisServer server) {

		// get network server properties
		int writeBufferSize = Integer.parseInt(server.getProperties().getProperty("ns.writeBuffer"));
		int objectBufferSize = Integer.parseInt(server.getProperties().getProperty("ns.objectBuffer"));

		// create network server
		Server networkServer = new Server(writeBufferSize, objectBufferSize) {

			@Override
			protected Connection newConnection() {
				return new ClientConnection();
			}
		};

		// register objects to be sent over network
		Registry.register(networkServer);

		// add equinoxServer.client handler (listener)
		networkServer.addListener(new ClientHandler(server));

		// log info
		server.getLogger().info("Network server setup.");

		// return server
		return networkServer;
	}

	/**
	 * Shuts down the given thread executor in two phases, first by calling shutdown to reject incoming tasks, and then calling shutdownNow, if necessary, to cancel any lingering tasks.
	 *
	 * @param executor
	 *            Thread executor to shutdown.
	 * @param logger
	 *            Server logger.
	 */
	public static void shutdownThreadPool(ExecutorService executor, Logger logger) {

		// disable new tasks from being submitted
		executor.shutdown();

		try {

			// wait a while for existing tasks to terminate
			if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {

				// cancel currently executing tasks
				executor.shutdownNow();

				// wait a while for tasks to respond to being canceled
				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
					logger.warning("Thread pool " + executor.toString() + " did not terminate.");
				}
			}
		}

		// exception occurred during shutting down the thread pool
		catch (InterruptedException ie) {

			// cancel if current thread also interrupted
			executor.shutdownNow();

			// preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Creates and returns working directory for the task.
	 *
	 * @param namePrefix
	 *            Prefix for the directory name.
	 * @return Path to newly created working directory.
	 * @throws IOException
	 *             If exception occurs during process.
	 */
	public static Path createWorkingDirectory(String namePrefix) throws IOException {
		Path workingDirectory = Paths.get(namePrefix + "_" + RandomUtils.nextInt(0, 10000));
		while (Files.exists(workingDirectory)) {
			workingDirectory = Paths.get(namePrefix + "_" + RandomUtils.nextInt(0, 10000));
		}
		return Files.createDirectory(workingDirectory);
	}

	/**
	 * Builds and returns a new connection to filer SFTP server.
	 *
	 * @param logger
	 *            Server logger.
	 * @return Filer connection. parameters.
	 * @throws JSchException
	 *             If filer connection cannot be established.
	 */
	public static FilerConnection createFilerConnection(Logger logger) throws JSchException {

		// set connection properties
		String username = System.getenv("sftp.username");
		String hostname = System.getenv("sftp.hostname");
		int port = Integer.parseInt(System.getenv("sftp.port"));
		String password = System.getenv("sftp.password");
		String filerRoot = System.getenv("sftp.rootPath");

		// create session
		JSch jsch = new JSch();
		Session session = jsch.getSession(username, hostname, port);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
		session.setPassword(password);
		session.connect();

		// open channel and connect
		Channel channel = session.openChannel("sftp");
		channel.connect();
		ChannelSftp sftpChannel = (ChannelSftp) channel;

		// create and return connection object
		return new FilerConnection(session, channel, sftpChannel, logger, filerRoot);
	}

	/**
	 * Returns server properties.
	 *
	 * @param propertyFile
	 *            Path to property file.
	 * @return Server properties.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	public static Properties loadProperties(Path propertyFile) throws Exception {

		// create encrypted properties
		// OFF Properties props = new EncryptableProperties(PROPERTY_ENCRYPTOR);
		Properties props = new Properties();

		// load properties
		try (InputStream input = Files.newInputStream(propertyFile)) {
			props.load(input);
		}

		// return properties
		return props;
	}

	/**
	 * Extracts and returns all files from the given ZIP file.
	 *
	 * @param zipFile
	 *            Path to ZIP file.
	 * @param outputDir
	 *            Output directory.
	 * @return The extracted temporary files or null if no file could be found within the given ZIP file.
	 * @throws IOException
	 *             If exception occurs during process.
	 */
	public static ArrayList<Path> extractAllFilesFromZIP(Path zipFile, Path outputDir) throws IOException {

		// initialize output file
		ArrayList<Path> output = null;

		// create zip input stream
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile.toString())), Charset.defaultCharset())) {

			// loop over zip entries
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {

				// not directory
				if (!ze.isDirectory()) {

					// create temporary output file
					Path file = outputDir.resolve(ze.getName());

					// create all necessary directories
					Path fileParentDir = file.getParent();
					if (fileParentDir != null) {
						Files.createDirectories(fileParentDir);
					}

					// create output stream
					try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.toString()))) {

						// create new buffer
						byte[] buffer = new byte[BUFSIZE];

						// write to output stream
						int len;
						while ((len = zis.read(buffer, 0, BUFSIZE)) != -1) {
							bos.write(buffer, 0, len);
						}
					}

					// file is directory, doesn't exist or hidden
					if (!Files.exists(file) || Files.isDirectory(file) || Files.isHidden(file) || !Files.isRegularFile(file)) {
						continue;
					}

					// add file to output
					if (output == null) {
						output = new ArrayList<>();
					}
					output.add(file);
				}

				// close entry
				zis.closeEntry();
			}
		}

		// return output file
		return output;
	}

	/**
	 * Zips given files to given output file.
	 *
	 * @param files
	 *            Files to zip.
	 * @param output
	 *            Output file path.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	public static void zipFiles(ArrayList<Path> files, File output) throws Exception {

		// create zip output stream
		try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {

			// create buffer to store to be written bytes
			byte[] buf = new byte[1024];

			// loop over input files
			for (Path file : files) {

				// get file name
				Path fileName = file.getFileName();
				if (fileName == null)
					throw new Exception("Cannot get file name.");

				// zip file
				zipFile(file, fileName.toString(), zos, buf);
			}
		}
	}

	/**
	 * Zips given file recursively.
	 *
	 * @param path
	 *            Path to file.
	 * @param name
	 *            Name of file.
	 * @param zos
	 *            Zip output stream.
	 * @param buf
	 *            Byte buffer.
	 * @throws Exception
	 *             If exception occurs during process.
	 */
	private static void zipFile(Path path, String name, ZipOutputStream zos, byte[] buf) throws Exception {

		// directory
		if (Files.isDirectory(path)) {

			// create and close new zip entry
			zos.putNextEntry(new ZipEntry(name + "/"));
			zos.closeEntry();

			// create directory stream
			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {

				// get iterator
				Iterator<Path> iterator = dirStream.iterator();

				// loop over files
				while (iterator.hasNext()) {

					// get file
					Path file = iterator.next();

					// get file name
					Path fileName = file.getFileName();
					if (fileName == null)
						throw new Exception("Cannot get file name.");

					// zip file
					zipFile(file, name + "/" + fileName.toString(), zos, buf);
				}
			}
		}

		// file
		else {

			// create new zip entry
			zos.putNextEntry(new ZipEntry(name));

			// create stream to read file
			try (FileInputStream fis = new FileInputStream(path.toString())) {

				// read till the end of file
				int len;
				while ((len = fis.read(buf)) > 0) {
					zos.write(buf, 0, len);
				}
			}

			// close zip entry
			zos.closeEntry();
		}
	}
}