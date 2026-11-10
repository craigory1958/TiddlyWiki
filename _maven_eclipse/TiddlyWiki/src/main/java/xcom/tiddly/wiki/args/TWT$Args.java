

package xcom.tiddly.wiki.args ;


import static xcom.tiddly.wiki.args.TWT$Props.$Prop_ArchiveDefaultTargetBSpec ;
import static xcom.tiddly.wiki.args.TWT$Props.$Prop_BaseDefaultSourceBSpec ;
import static xcom.tiddly.wiki.args.TWT$Props.$Prop_ProvisionBuildsFolder ;

import java.nio.file.FileSystems ;
import java.util.Map ;
import java.util.Properties ;

import org.apache.commons.cli.CommandLine ;
import org.apache.commons.cli.Option ;
import org.apache.commons.cli.Options ;
import org.apache.commons.lang3.StringUtils ;
import org.slf4j.Logger ;

import xcom.utils4j.CLArgs ;
import xcom.utils4j.logging.aspects.api.annotations.Log ;


@Log
public class TWT$Args extends CLArgs {

	static final String PathSeparator = FileSystems.getDefault().getSeparator() ;


	/**
	 * The valid command line options.
	 */

	public static final String $Arg_Archive = "a" ;
	public static final String $ArgLong_Archive = "archive" ;
	@SuppressWarnings("deprecation")
	public static final Option $ArgOption_Archive = Option.builder($Arg_Archive).longOpt($ArgLong_Archive).hasArg(true) //
			.argName("dSpec").desc("Archive").build() ;

	public static final String $Arg_Builds = "b" ;
	public static final String $ArgLong_Builds = "builds" ;
	@SuppressWarnings("deprecation")
	public static final Option $ArgOption_Builds = Option.builder($Arg_Builds).longOpt($ArgLong_Builds).hasArg(true) //
			.argName("fSpec [, fSpec]").desc("Build(s)").build() ;

	public static final String $ArgLong_Base = "base" ;
	@SuppressWarnings("deprecation")
	public static final Option $ArgOption_Base = Option.builder().longOpt($ArgLong_Base).hasArg(true) //
			.argName("dSpec").desc("EBB Templates").build() ;

	public static final String $Arg_Contexts = "ctx" ;
	public static final String $ArgLong_Contexts = "contexts" ;
	@SuppressWarnings("deprecation")
	public static final Option $ArgOption_Contexts = Option.builder($Arg_Contexts).longOpt($ArgLong_Contexts).hasArg(true) //
			.argName("fSpec [, fSpec]").desc("Context Merge Source(s)").build() ;

	public static final String $Arg_Filters = "f" ;
	public static final String $ArgLong_Filters = "filters" ;
	@SuppressWarnings("deprecation")
	public static final Option $ArgOption_Filters = Option.builder($Arg_Filters).longOpt($ArgLong_Filters).hasArg(true) //
			.argName("filter [, filter]").desc("Tiddler Filters").build() ;

	public static final String $ArgLong_Provision = "provision" ;
	@SuppressWarnings("deprecation")
	public static final Option $ArgOption_Provision = Option.builder().longOpt($ArgLong_Provision).hasArg(false) //
			.argName("dSpec").desc("Provision Source Directory").build() ;

	public static final String $Arg_TiddlyWiki = "tw" ;
	public static final String $ArgLong_TiddlyWiki = "tiddlywiki" ;
	@SuppressWarnings("deprecation")
	public static final Option $ArgOption_TiddlyWiki = Option.builder($Arg_TiddlyWiki).longOpt($ArgLong_TiddlyWiki).hasArg(true).optionalArg(true) //
			.argName("fSpec").desc("TiddlyWiki Destination File").build() ;

	public static final String $Arg_Title = "t" ;
	public static final String $ArgLong_Title = "title" ;
	@SuppressWarnings("deprecation")
	public static final Option $ArgOption_Title = Option.builder($Arg_Title).longOpt($ArgLong_Title).hasArg(true).optionalArg(true) //
			.desc("TiddlyWiki Title").build() ;


	public static final Options CommandLineOptions = new Options() ;
	static {
		CommandLineOptions.addOption(CLArgOption_Help) ;
		CommandLineOptions.addOption(CLArgOption_Log) ;
		CommandLineOptions.addOption(CLArgOption_Version) ;

		CommandLineOptions.addOption($ArgOption_Archive) ;
		CommandLineOptions.addOption($ArgOption_Base) ;
		CommandLineOptions.addOption($ArgOption_Builds) ;
		CommandLineOptions.addOption($ArgOption_Contexts) ;
		CommandLineOptions.addOption($ArgOption_Filters) ;
		CommandLineOptions.addOption($ArgOption_Provision) ;
		CommandLineOptions.addOption($ArgOption_TiddlyWiki) ;
		CommandLineOptions.addOption($ArgOption_Title) ;
	}


	public TWT$Args(final CommandLine cmd, final Properties props, final String[] args, final Logger logger) {
		super(cmd, props, args, logger) ;
	}


	//
	//
	//

	public String decodeArg_archive(final CLArgs $args, final CommandLine cmd, final Map<String, Object> decodedArgs, final Properties props,
			final Logger logger) {

		if ( !StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Archive)).isEmpty() )
			return StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Archive)) ;

		return props.getProperty($Prop_ArchiveDefaultTargetBSpec) ;
	}

	public String decodeArg_base(final CLArgs $args, final CommandLine cmd, final Map<String, Object> decodedArgs, final Properties props,
			final Logger logger) {

		if ( !StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Base)).isEmpty() )
			return StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Base)) ;

		return props.getProperty($Prop_BaseDefaultSourceBSpec) ;
	}


	public String decodeArg_builds(final CLArgs $args, final CommandLine cmd, final Map<String, Object> decodedArgs, final Properties props,
			final Logger logger) {

		return StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Builds)) ;
	}


	public String decodeArg_contexts(final CLArgs $args, final CommandLine cmd, final Map<String, Object> decodedArgs, final Properties props,
			final Logger logger) {

		return StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Contexts)) ;
	}


	public String decodeArg_filters(final CLArgs $args, final CommandLine cmd, final Map<String, Object> decodedArgs, final Properties props,
			final Logger logger) {

		return StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Filters)) ;
	}


	public String decodeArg_provision(final CLArgs $args, final CommandLine cmd, final Map<String, Object> decodedArgs, final Properties props,
			final Logger logger) {

		return StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Provision)) ;
	}


	public String decodeArg_tiddlywiki(final CLArgs $args, final CommandLine cmd, final Map<String, Object> decodedArgs, final Properties props,
			final Logger logger) {

		return StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_TiddlyWiki)) ;
	}


	public String decodeArg_title(final CLArgs $args, final CommandLine cmd, final Map<String, Object> decodedArgs, final Properties props,
			final Logger logger) {

		return StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Title)) ;
	}


	//
	//
	//

	public String decodeMiscArg_provision(final CommandLine cmd, final Map<String, Object> decodedArgs, final Properties props,
			final Logger logger) {

		if ( !StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Provision)).isEmpty() )
			return StringUtils.trimToEmpty(cmd.getOptionValue($ArgLong_Provision)) ;


		String builds = (String) decodedArgs.get($ArgLong_Builds) ;

		return builds.substring(0, builds.indexOf((String) props.get($Prop_ProvisionBuildsFolder)) - 1) ;
	}
}
