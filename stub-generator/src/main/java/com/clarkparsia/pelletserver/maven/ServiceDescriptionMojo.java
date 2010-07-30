package com.clarkparsia.pelletserver.maven;

import java.io.File;
import java.net.URL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.clarkparsia.pelletserver.scala.StubGenerator;

/**
 * Generate Scala source files declaring classes as defined
 * in a PelletServer ServiceDescription and RDF Schema.
 * 
 *  @goal generate
 *  @phase generate-sources
 *  @requiresDependencyResolution compile
 */
public class ServiceDescriptionMojo extends AbstractMojo {

    /**
     * The Maven Project Object
     *
     * @parameter expression="${project}" default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;
    
	/**
	 * Service description URL
	 * @parameter
	 */
	private URL serviceDescription;

	/**
	 * PelletServer schema URL
	 * @parameter
	 */
	private URL schema;
	
	/**
	 * Specifies the output directory where generated sources will be placed,
	 * defaults to target/generated-sources/scala-ps-stubgenerator
	 * @parameter expression="${project.build.directory}/generated-sources/scala-ps-stubgenerator"
     * @required
	 */
	protected File outputDirectory;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Generating Scala source from PelletServer ServiceDescription and RDF Schema");
		if (schema == null) {
			getLog().info("Using built-in schema.");
			schema = getClass().getResource("/schemas/ps.rdf");
			if (schema == null) {
				throw new MojoFailureException("Couldn't find built-in schema.");
			}
		}
		StubGenerator.loadSchema(schema);
		File directoryWithPackage = new File(outputDirectory, StubGenerator.PACKAGE().replace('.', File.separatorChar));
		StubGenerator.setOutputDirectory(directoryWithPackage);
		StubGenerator.generateAbstractHierarchy();
		if (serviceDescription == null) {
			getLog().info("Using built-in service description.");
			serviceDescription = getClass().getResource("/root-sd.json");
			if (serviceDescription == null) {
				throw new MojoFailureException("Couldn't find built-in service description.");
			}
		}
		StubGenerator.generateMethods(serviceDescription);
		StubGenerator.generateExtractionClasses();
		if (project != null) {
			getLog().info("Adding generated source folder " + outputDirectory);
			project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
		} else {
			getLog().debug("Couldn't add source folder");
		}
	}

}
