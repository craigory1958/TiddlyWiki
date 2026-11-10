

package xcom.tiddly.wiki ;


import static xcom.tiddly.wiki.args.TWT$Args.$ArgLong_Archive ;
import static xcom.tiddly.wiki.args.TWT$Args.$ArgLong_Base ;
import static xcom.tiddly.wiki.args.TWT$Args.$ArgLong_Contexts ;
import static xcom.tiddly.wiki.args.TWT$Args.$ArgLong_Filters ;
import static xcom.tiddly.wiki.args.TWT$Args.$ArgLong_Provision ;
import static xcom.tiddly.wiki.args.TWT$Args.$ArgLong_TiddlyWiki ;
import static xcom.tiddly.wiki.args.TWT$Args.$ArgLong_Title ;
import static xcom.tiddly.wiki.args.TWT$Args.CommandLineOptions ;
import static xcom.tiddly.wiki.args.TWT$Props.$Prop_ProvisionBuildsFolder ;
import static xcom.tiddly.wiki.args.TWT$Props.$Prop_ProvisionResourceFolders ;
import static xcom.tiddly.wiki.args.TWT$Props.$Prop_TagOpenDelimiter ;
import static xcom.tiddly.wiki.args.TWT$Props.$Prop_TiddlyWikiPropertiesFilename ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_CreatedAtributeName ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_InjectTiddleWikiValuePrefix ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_MergedContentTagCloseDelimiter ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_MergedContentTagOpenDelimiter ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_ModifiedAtributeName ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_StatusColumnName ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_StatusColumn_SkipValue ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_TagCloseDelimiter ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_TagOpenDelimiter ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_TemplateBasename ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_TiddlerFSpec ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_TiddlerIndexTag ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_TiddlerIndexTemplateBasename ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_TiddlersTag ;
import static xcom.tiddly.wiki.args.TiddlyWiki$Props.TW$Prop_TitleAtributeName ;
import static xcom.tiddly.wiki.misc.TiddlerFilter.CopyContextTypes.CopyAllTaggedContext ;
import static xcom.tiddly.wiki.misc.TiddlerFilter.CopyContextTypes.CopyFilteredContextOnly ;
import static xcom.tiddly.wiki.misc.TiddlerFilter.CopyContextTypes.CopyTaggedContextOnly ;
import static xcom.utils4j.CLArgs.logEvent ;
import static xcom.utils4j.CLArgs.ExitPolicyTypes.ExitOnHelpOrVersion ;
import static xcom.utils4j.logging.Loggers.ConsoleLoggerName ;
import static xcom.utils4j.resources.Props.PropagateExceptionTypes.DoNotPropagateException ;

import java.io.File ;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.net.URISyntaxException ;
import java.nio.charset.StandardCharsets ;
import java.nio.file.DirectoryStream ;
import java.nio.file.FileSystems ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.PathMatcher ;
import java.nio.file.Paths ;
import java.time.LocalDateTime ;
import java.time.format.DateTimeFormatter ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;
import java.util.Properties ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;
import java.util.stream.Collectors ;
import java.util.stream.Stream ;
import java.util.stream.StreamSupport ;

import javax.xml.parsers.ParserConfigurationException ;

import org.apache.commons.cli.CommandLine ;
import org.apache.commons.cli.ParseException ;
import org.apache.commons.io.FileUtils ;
import org.apache.commons.io.FilenameUtils ;
import org.apache.commons.lang3.StringEscapeUtils ;
import org.apache.commons.lang3.StringUtils ;
import org.apache.poi.EncryptedDocumentException ;
import org.apache.poi.ss.usermodel.Row ;
import org.apache.poi.ss.usermodel.Sheet ;
import org.apache.poi.ss.usermodel.Workbook ;
import org.apache.poi.ss.usermodel.WorkbookFactory ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import org.xml.sax.SAXException ;

import xcom.tiddly.wiki.args.TWT$Args ;
import xcom.tiddly.wiki.misc.TiddlerFilter ;
import xcom.tiddly.wiki.misc.TiddlerFilter.CopyContextTypes ;
import xcom.utils4j.CLArgs ;
import xcom.utils4j.data.Excels ;
import xcom.utils4j.format.Templator ;
import xcom.utils4j.format.Templator.Templator$Delimited ;
import xcom.utils4j.logging.aspects.api.annotations.Log ;
import xcom.utils4j.resources.Props ;


/**
 * <pre>
 *
 * usage: java -cp twt.jar xcom.tiddly.wiki.TWT [options]
 *
 * TWT - TiddlyWiki Tool
 *
 * Options:
 *     -a,--archive <dSpec>                Archive
 *     -b,--builds <fSpec [, fSpec]>       Build(s)
 *        --base <dSpec>                   EBB Templates
 *     -ctx,--contexts <fSpec [, fSpec]>   Context Merge Source(s)
 *     -f,--filters <filter [, filter]>    Tiddler Filters
 *     -h,--help                           Display this info
 *        --log <{event} [, {event} ...]>  Logging events
 *        --provision                      Provision Source Directory
 *     -tw,--tiddlywiki <fSpec>            TiddlyWiki Destination File
 *        --version                        Display version info
 *
 * See https://github.com/craigory1958/Plastic-Modeling
 *
 * </pre>
 *
 * @author Craig Gregory
 *
 */
@Log
public class TWT {

	private static final Logger Logger = LoggerFactory.getLogger(TWT.class) ;
	private static final Logger Console = LoggerFactory.getLogger(ConsoleLoggerName) ;


	static final String AppClassname = TWT.class.getName() ;
	static final String AppDesc = TWT.class.getSimpleName() + " - TiddlyWiki Tool" ;
	static final String AppName = TWT.class.getSimpleName() ;
	static final String AppSee = "See https://github.com/craigory1958/Plastic-Modeling" ;
	static final String AppUsage = "java -cp " + TWT.class.getSimpleName().toLowerCase() + ".jar " + TWT.class.getName() + " [options]" ;
	static final String AppVersion = "v1.0.0" ;

	static final String PathSeparator = FileSystems.getDefault().getSeparator() ;


	TWT$Args args ;
	Map<String, Object> decodedArgs ;
	Properties props ;

	String twt$date_yycc_mm ;

	Templator$Delimited cmdTaggedTemplate ;
	TiddlerFilter filter ;
	List<String> generatedBuilds ;
	LocalDateTime now ;
	String statusColumn_Name ;
	String statusColumn_SkipValue ;

	List<Path> buildsTiddlyWikis ;
	List<Path> buildsWorkbooks ;
	List<Path> contextsTiddlyWikis ;
	List<Path> contextsWorkbooks ;

	Map<String, Object> twInjectValues ;
	String twCreated ;
	String twCreatedAtributeName ;
	StringBuilder twDocument ;
	String twModified ;
	String twModifiedAtributeName ;
	Templator$Delimited twTaggedTemplate ;
	Map<String, StringBuilder> twTiddlers ;
	List<StringBuilder> twTiddlerIndexes ;
	StringBuilder twtTiddlerIndexTemplate ;
	String twTitleAtributeName ;


	//
	//
	//

	public TWT() throws IOException, URISyntaxException {

		initialize(this) ;
	}


	public TWT(final TWT$Args args, final Map<String, Object> decodedArgs, final Properties props) throws IOException {

		this.args = args ;
		this.decodedArgs = decodedArgs ;
		this.props = props ;

		initialize(this) ;
	}


	TWT initialize(final TWT $) throws IOException {

		$.now = LocalDateTime.now() ;

		$.twt$date_yycc_mm = $.now.format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) ;
		$.props.put("twt$date_yycc_mm", twt$date_yycc_mm) ;

		$.statusColumn_Name = $.props.getProperty(TW$Prop_StatusColumnName) ;
		$.statusColumn_SkipValue = $.props.getProperty(TW$Prop_StatusColumn_SkipValue) ;

		$.buildsWorkbooks = new ArrayList<>() ;
		$.buildsTiddlyWikis = new ArrayList<>() ;

		$.contextsWorkbooks = new ArrayList<>() ;
		$.contextsTiddlyWikis = new ArrayList<>() ;

		$.twTitleAtributeName = $.props.getProperty(TW$Prop_TitleAtributeName) ;
		$.twCreatedAtributeName = $.props.getProperty(TW$Prop_CreatedAtributeName) ;
		$.twModifiedAtributeName = $.props.getProperty(TW$Prop_ModifiedAtributeName) ;


		$.twCreated = $.now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSS")) ;
		$.twModified = $.twCreated ;

		$.twInjectValues = new HashMap<>() ;
		$.twTiddlers = new HashMap<>() ;
		$.twTiddlerIndexes = new ArrayList<>() ;


		// Process filters ...
		{
			$.filter = new TiddlerFilter($.args, StringUtils.trimToEmpty($.args.cmd().getOptionValue($ArgLong_Filters))) ;
			$.decodedArgs.put($ArgLong_Filters, StringUtils.trimToEmpty($.args.cmd().getOptionValue($ArgLong_Filters))) ;
			logEvent($.args, "init", "filters: {}", $.decodedArgs.get($ArgLong_Filters)) ;
		}


		// Initialize templators ...
		{
			$.cmdTaggedTemplate = Templator.delimiters(Templator.UnixDelimiters) ;

			$.twTaggedTemplate = Templator.delimiters(Templator.DefaultDelimiters) ;
			$.twTaggedTemplate.openTagDelimiter($.props.getProperty(TW$Prop_TagOpenDelimiter)) ;
			$.twTaggedTemplate.closeTagDelimiter($.props.getProperty(TW$Prop_TagCloseDelimiter)) ;
		}


		//
		{
			String fSpec = "${base}/" + $.props.getProperty(TW$Prop_TiddlerIndexTemplateBasename) ;
			fSpec = replaceCmdTags(fSpec, $.decodedArgs) ;
			logEvent($.args, "init", "tw.tiddler.index.template: {}", fSpec) ;

			$.twtTiddlerIndexTemplate = new StringBuilder(FileUtils.readFileToString(new File(fSpec), StandardCharsets.UTF_8)) ;

			Logger.debug("tiddlerIndexTemplate: |{}|", $.twtTiddlerIndexTemplate) ;
		}


		return this ;
	}


	//
	//
	//

	TWT archive() {

		if ( args.cmd().hasOption($ArgLong_Archive) )
			Console.info("\nArchiving ...") ;


		return this ;
	}


	//
	//
	//

	TWT assemble() throws IOException {

		// Assemble TiddlyWiki ...

		Console.info("\nAssembling TiddlyWiki ...") ;

		final Map<String, Object> injectValues = new HashMap<>(twInjectValues) ;


		{
			Logger.debug("twtTiddlerIndexes {}", twTiddlerIndexes) ;

			final StringBuilder sb = new StringBuilder() ;
			for ( final StringBuilder tiddlerIndex : twTiddlerIndexes )
				sb.append(tiddlerIndex) ;
			injectValues.put(props.getProperty(TW$Prop_TiddlerIndexTag), sb.toString()) ;
		}


		{
			final StringBuilder sb = new StringBuilder() ;
			for ( final StringBuilder tiddler : twTiddlers.values() )
				sb.append(tiddler) ;
			injectValues.put(props.getProperty(TW$Prop_TiddlersTag), sb.toString()) ;
		}


		{
			final String fSpec = decodedArgs.get($ArgLong_Base) + PathSeparator + props.getProperty(TW$Prop_TemplateBasename) ;
			Logger.debug("Template: |{}|", fSpec) ;
			Console.info("    assemble template @{}", fSpec) ;

			final StringBuilder template = new StringBuilder(FileUtils.readFileToString(new File(fSpec), StandardCharsets.UTF_8)) ;

			twDocument = twTaggedTemplate.template(template).inject(injectValues) ;
		}


		return this ;
	}


	//
	//
	//

	TWT copy() throws IOException {

		if ( args.cmd().hasOption($ArgLong_Contexts) ) {

			Console.info("\nCopying context ...") ;


			for ( final Path _tiddlyWiki : contextsTiddlyWikis ) {
				final String fSpec = _tiddlyWiki.toFile().getPath() ;
				Console.info("    copy context @{}", fSpec) ;

				final StringBuilder context = new StringBuilder(FileUtils.readFileToString(new File(fSpec), StandardCharsets.UTF_8)) ;
				copy(context, twTiddlers, twtTiddlerIndexTemplate, twTiddlerIndexes, CopyTaggedContextOnly, decodedArgs, props) ;
			}


			for ( final Path _tiddlyWiki : buildsTiddlyWikis ) {
				final String fSpec = _tiddlyWiki.toFile().getPath() ;
				Console.info("    copy context @{}", fSpec) ;

				final StringBuilder context = new StringBuilder(FileUtils.readFileToString(new File(fSpec), StandardCharsets.UTF_8)) ;

				CopyContextTypes copyType = (args.cmd().hasOption($ArgLong_Filters) ? CopyFilteredContextOnly : CopyAllTaggedContext) ;
				copy(context, twTiddlers, twtTiddlerIndexTemplate, twTiddlerIndexes, copyType, decodedArgs, props) ;
			}
		}


		Console.info("    Total Tiddlers {}", twTiddlers.size()) ;
		Console.info("    Tiddler Indexes {}", twTiddlerIndexes.size()) ;

		return this ;
	}


	TWT copy(final StringBuilder context, final Map<String, StringBuilder> tiddlers, final StringBuilder tiddlerIndexTemplate,
			final List<StringBuilder> tiddlerIndexes, CopyContextTypes copyType, final Map<String, Object> cmdArgs, final Properties props) {

		final Map<String, Object> values = new HashMap<>() ;

		final String titleRegEx = "title=\"(.*?)\"" ;
		final Pattern titlePattern = Pattern.compile(titleRegEx, Pattern.DOTALL | Pattern.CASE_INSENSITIVE) ;
		Logger.debug("titleRegEx: |{}|", titleRegEx) ;


		int cntTotal = 0 ;
		int cntProcessed = 0 ;

		//
		// Copy tiddlers from context with a 'twt-copy' tag and 'twt-copy-selector' matching filter into generated tiddlers ...
		//
		{
			final String twtCopyRegEx = " tags=\".*?twt-copy.*?\" " ;    // Look for div's with 'twt-copy' tag.
			Logger.debug("twtCopyRegEx: |{}|", twtCopyRegEx) ;

			final Matcher twtCopyMatcher = Pattern.compile(twtCopyRegEx, Pattern.CASE_INSENSITIVE).matcher(context) ;

			while ( twtCopyMatcher.find() ) {
				cntTotal++ ;

				Logger.debug("copyMatcher.group(): |{}|", twtCopyMatcher.group()) ;

				int start = twtCopyMatcher.start() ;
				int stop = twtCopyMatcher.end() ;

				// Backup to start of <div ...
				while ( !context.substring(start, start + 4).equalsIgnoreCase("<div") )
					start-- ;

				// Advanced to end of <div ...
				while ( !context.substring(stop - 6, stop).equalsIgnoreCase("</div>") )
					stop++ ;

				final StringBuilder twtCopy = new StringBuilder(context.substring(start, stop)) ;
				logEvent(args, "copy", "twtCopy: {} - |{}|", twtCopy.toString().length(), twtCopy.substring(50).replaceAll("[\\n\\r]", "")) ;


				if ( filter.matchesCopyContextType(twtCopy, copyType) ) {
					cntProcessed++ ;

					final Matcher titleMatcher = titlePattern.matcher(new StringBuilder(context.substring(start, stop))) ;

					titleMatcher.find() ;
					final String _title = titleMatcher.group(1) ;
					logEvent(args, "copy", "twtCopy title: {}", _title) ;

					tiddlers.put(_title, twtCopy) ;

					values.put(twTitleAtributeName, _title) ;
					tiddlerIndexes.add(twTaggedTemplate.template(tiddlerIndexTemplate).inject(values)) ;
				}
			}
		}


		Console.info("        {} of {} contexts copied.", cntProcessed, cntTotal) ;

		return this ;
	}


	//
	//
	//

	TWT extract() throws IOException {

		Console.info("\nExtracing 'merged-content' from TiddlyWiki(s)  ...") ;


		for ( final Path _tiddlyWiki : contextsTiddlyWikis ) {
			final String fSpec = _tiddlyWiki.toFile().getPath() ;
			Console.info("    extract @{}", fSpec) ;

			final StringBuilder content = new StringBuilder(FileUtils.readFileToString(new File(fSpec), StandardCharsets.UTF_8)) ;
			extract(content, twTiddlers, twtTiddlerIndexTemplate, twTiddlerIndexes, decodedArgs, props) ;
		}


		for ( final Path _tiddlyWiki : buildsTiddlyWikis ) {
			final String fSpec = _tiddlyWiki.toFile().getPath() ;
			Console.info("    extract @{}", fSpec) ;

			final StringBuilder content = new StringBuilder(FileUtils.readFileToString(new File(fSpec), StandardCharsets.UTF_8)) ;
			extract(content, twTiddlers, twtTiddlerIndexTemplate, twTiddlerIndexes, decodedArgs, props) ;
		}


		return this ;
	}


	TWT extract(final StringBuilder content, final Map<String, StringBuilder> tiddlers, final StringBuilder tiddlerIndexTemplate,
			final List<StringBuilder> tiddlerIndexes, final Map<String, Object> cmdArgs, final Properties props) {

		final String titleRegEx = "title=\"(.*?)\"" ;
		Logger.debug("titleRegEx: |{}|", titleRegEx) ;


		int cntTotal = 0 ;
		int cntProcessed = 0 ;

		//
		// Extract any 'merged-content' from a TiddlyWiki and inject into corresponding generated tiddlers ...
		//
		{
			final String openContentTag = StringEscapeUtils.escapeXml10(props.getProperty(TW$Prop_MergedContentTagOpenDelimiter)) ;
			final String closeContentTag = StringEscapeUtils.escapeXml10(props.getProperty(TW$Prop_MergedContentTagCloseDelimiter)) ;

			final String sourceRegEx = openContentTag + "(.*?)" + closeContentTag + ".*?</div>" ;
			Logger.debug("sourceRegEx: |{}|", sourceRegEx) ;

			final Pattern sourcePattern = Pattern.compile(sourceRegEx, Pattern.DOTALL | Pattern.CASE_INSENSITIVE) ;
			final Matcher sourceMatcher = sourcePattern.matcher(content) ;


			final String targetRegEx = openContentTag + "(.*?)" + closeContentTag ;
			Logger.debug("targetRegEx: |{}|", targetRegEx) ;

			final Pattern targetPattern = Pattern.compile(targetRegEx, Pattern.DOTALL | Pattern.CASE_INSENSITIVE) ;


			while ( sourceMatcher.find() ) {
				cntTotal++ ;

				int start = sourceMatcher.start() ;
				final int stop = sourceMatcher.end() ;

				// Backup to start of div ...
				while ( !content.substring(start, start + 4).equalsIgnoreCase("<div") )
					start-- ;

				final Pattern titlePattern = Pattern.compile(titleRegEx, Pattern.DOTALL | Pattern.CASE_INSENSITIVE) ;
				final Matcher titleMatcher = titlePattern.matcher(new StringBuilder(content.substring(start, stop))) ;

				titleMatcher.find() ;
				final String _title = titleMatcher.group(1) ;
				Logger.debug("_title: |{}|", _title) ;
				Logger.debug("sourceMatcher.group(1): |{}|", sourceMatcher.group(1)) ;


				if ( tiddlers.containsKey(_title) ) {
					cntProcessed++ ;

					final StringBuilder tiddler = tiddlers.get(_title) ;
					final Matcher targetMatcher = targetPattern.matcher(tiddler) ;

					targetMatcher.find() ;
					Logger.debug("targetMatcher.group(1): |{}|", targetMatcher.group(1)) ;

					tiddler.replace(targetMatcher.start(1), targetMatcher.end(1), sourceMatcher.group(1)) ;
					tiddlers.put(_title, tiddler) ;
				}
			}
		}


		Console.info("        {} of {} contents extracted.", cntProcessed, cntTotal) ;

		return this ;
	}


	//
	//
	//

	TWT generate() throws EncryptedDocumentException, IOException {

		Console.info("\nGenerating Tiddlers from Workbook(s) ...") ;

		generatedBuilds = new ArrayList<>() ;


		for ( final Path _workbook : contextsWorkbooks ) {

			final String fSpec = _workbook.toFile().getPath() ;
			Console.info("    workbook @{}", fSpec) ;

			final Workbook workbook = WorkbookFactory.create(new File(fSpec), null, true) ;

			generate(workbook, twTiddlers, twtTiddlerIndexTemplate, twTiddlerIndexes, twInjectValues, decodedArgs, props) ;

			workbook.close() ;
		}


		for ( final Path _workbook : buildsWorkbooks ) {

			final String fSpec = _workbook.toFile().getPath() ;
			Console.info("    workbook @{}", fSpec) ;

			final Workbook workbook = WorkbookFactory.create(new File(fSpec), null, true) ;

			generate(workbook, twTiddlers, twtTiddlerIndexTemplate, twTiddlerIndexes, twInjectValues, decodedArgs, props) ;

			workbook.close() ;
		}


		Logger.debug("values: {}", twInjectValues) ;
		Logger.debug("tiddlers: {}", twTiddlers) ;
		Logger.debug("tiddler indexes: {}", twTiddlerIndexes) ;

		Console.info("    Total Tiddlers {}", twTiddlers.size()) ;
		Console.info("    Tiddler Indexes {}", twTiddlerIndexes.size()) ;

		return this ;
	}


	TWT generate(final Workbook workbook, final Map<String, StringBuilder> tiddlers, final StringBuilder tiddlerIndexTemplate,
			final List<StringBuilder> tiddlerIndexes, final Map<String, Object> injectValues, final Map<String, Object> cmdArgs, final Properties props)
			throws IOException {

		//
		// Add TiddleWiki customized settings to values map ...
		//
		{
			final String prefix = props.getProperty(TW$Prop_InjectTiddleWikiValuePrefix) ;

			for ( final Entry<Object, Object> prop : props.entrySet() ) {
				final String key = (String) prop.getKey() ;

				if ( key.startsWith(prefix) && !key.equals(TW$Prop_InjectTiddleWikiValuePrefix) ) {

					final String[] tokens = ((String) prop.getValue()).split(",") ;

					String key_ = StringUtils.trimToEmpty(tokens[0]) ;
					final String value_ = (args.cmd().hasOption($ArgLong_Title) && key_.equals("TiddlyWiki-Subtitle")  //
							? (String) decodedArgs.get($ArgLong_Title)
							: StringUtils.trimToEmpty(tokens[1])) ;
					Logger.debug("prop: {}=|{}|", key_, value_) ;

					injectValues.put(key_, value_) ;
				}
			}
		}


		//
		// Process each sheet in workbook ...
		//
		{
			for ( int sIdx = 0; (sIdx < workbook.getNumberOfSheets()); sIdx++ ) {

				final Sheet sheet = workbook.getSheetAt(sIdx) ;
				String sheetName = sheet.getSheetName() ;


				// Decode sheet type from sheet name ...

				String sheetType = "" ;
				if ( "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(sheetName.substring(0, 1).toUpperCase()) == -1 ) {
					sheetType = sheetName.substring(0, 1) ;
					sheetName = sheetName.substring(1) ;
				}


				// Process sheet based on sheet type ...

				boolean matchAll = false ;

				switch ( sheetType ) {

					case "!":    // Skip processing of sheet.
						break ;


					case "@":    // Match all rows in sheet.
						matchAll = true ;

					case "": {    // Match rows based of filter.

						final HashMap<String, Object> parms = new HashMap<>(cmdArgs) ;
						parms.put("tiddler", sheetName) ;
						final String fSpec = replaceCmdTags(props.getProperty(TW$Prop_TiddlerFSpec), parms) ;
						Logger.debug("tiddler.template: {}", fSpec) ;

						final StringBuilder tiddlerTemplate = new StringBuilder(FileUtils.readFileToString(new File(fSpec), StandardCharsets.UTF_8)) ;
						Logger.debug("tiddlerTemplate: |{}|", tiddlerTemplate) ;

						generateTiddlersFromSheet(sheet, sheetName, tiddlerTemplate, tiddlers, tiddlerIndexTemplate, tiddlerIndexes, props, matchAll) ;

						break ;
					}
				}
			}

			Logger.debug("values: {}", injectValues) ;
		}


		return this ;
	}


	TWT generateTiddlersFromSheet(final Sheet sheet, final String sheetName, final StringBuilder tiddlerTemplate, final Map<String, StringBuilder> tiddlers,
			final StringBuilder tiddlerIndexTemplate, final List<StringBuilder> tiddlerIndexes, final Properties props, final boolean matchAll) {

		Console.info("        {} ...", sheet.getSheetName()) ;

		int cntTotal = 0 ;
		int cntProcessed = 0 ;

		final Map<String, Short> headers = Excels.readColumnHeadersAndIndex(sheet, true) ;

		final Map<String, Object> values = new HashMap<>() ;
		values.put(twCreatedAtributeName, twCreated) ;
		values.put(twModifiedAtributeName, twModified) ;

		for ( int rIdx = 1; (rIdx <= sheet.getLastRowNum()); rIdx++ ) {    // Skip row 0 (column headers).
			final Row row = sheet.getRow(rIdx) ;

			if ( (row == null) || StringUtils.trimToEmpty(Excels.getCellValueAsString(row.getCell(headers.get(twTitleAtributeName)))).isEmpty() )
				continue ;


			cntTotal++ ;

			for ( final Entry<String, Short> header : headers.entrySet() )
				if ( !header.getKey().startsWith("!") )
					values.put(header.getKey(),
							StringEscapeUtils.escapeXml10(StringUtils.trimToEmpty(Excels.getCellValueAsString(row.getCell(header.getValue()))))) ;

			Logger.debug("values: {}", values) ;

			if ( !((String) values.get(statusColumn_Name)).equalsIgnoreCase(statusColumn_SkipValue) )
				if ( matchAll || filter.matchesWorkbookValue(headers, values) ) {
					cntProcessed++ ;

					if ( tiddlers.containsKey(values.get(twTitleAtributeName)) )
						Logger.error("duplicate row {} - [{}]", rIdx, values) ;

					tiddlers.put((String) values.get(twTitleAtributeName), twTaggedTemplate.template(tiddlerTemplate).inject(values)) ;
					Logger.debug("tiddler: |{}|", values.get(twTitleAtributeName)) ;
					tiddlerIndexes.add(twTaggedTemplate.template(tiddlerIndexTemplate).inject(values)) ;

					if ( sheetName.equalsIgnoreCase("builds") )
						generatedBuilds.add(coerceGroupPath((String) values.get("affinity"), (String) values.get("file-name"))) ;
				}
		}


		Console.info("            {} of {} tiddlers generated.", cntProcessed, cntTotal) ;

		return this ;
	}


	static String coerceGroupPath(String affinity, String filename) {
		return (affinity.isEmpty() ? filename : affinity + PathSeparator + filename) ;
	}


	//
	//
	//

	TWT provision() throws IOException {

		if ( args.cmd().hasOption($ArgLong_Provision) ) {

			Console.info("\nProvisioning ...") ;

			String buildsBaseSourceDSpec = (String) decodedArgs.get($ArgLong_Provision) ;
			String buildsBaseTargetDSpec = FilenameUtils.getFullPathNoEndSeparator((String) decodedArgs.get($ArgLong_TiddlyWiki)) ;
			String buildsFolder = props.getProperty($Prop_ProvisionBuildsFolder) ;
			String[] resourceFolders = props.getProperty($Prop_ProvisionResourceFolders).split("[,]") ;

			Console.info("    @{} ...", decodedArgs.get($ArgLong_Provision)) ;


			for ( String build : generatedBuilds ) {

				Console.info("        {} ...", build) ;

				for ( String resource : resourceFolders )

					FileUtils.copyDirectory( //
							new File(buildsBaseSourceDSpec + "/" + buildsFolder + "/" + build + "/" + resource),
							new File(buildsBaseTargetDSpec + "/" + buildsFolder + "/" + build + "/" + resource) //
					) ;
			}
		}


		return this ;
	}


	//
	//
	//

	TWT scavage() throws IOException {

		Console.info("\nScavaging ...") ;

		{
			final List<String> srchSpecs = Arrays.asList(args.cmd().getOptionValue(TWT$Args.$ArgOption_Builds).split(",")) ;
			logEvent(args, "scavage", "srchSpecs: {}", srchSpecs) ;

			for ( final String _srchSpec : srchSpecs ) {
				Console.info("    Scavaging builds @{} ...", _srchSpec) ;

				buildsWorkbooks = scavageSearchPath(_srchSpec, "xlsx", buildsWorkbooks) ;
				buildsTiddlyWikis = scavageSearchPath(_srchSpec, "html", buildsTiddlyWikis) ;
			}

			Console.info("    Scavaged Workbooks: {}, TiddlyWikis: {}", buildsWorkbooks.size(), buildsTiddlyWikis.size()) ;
		}

		{
			final List<String> srchSpecs = Arrays.asList(args.cmd().getOptionValue(TWT$Args.$ArgOption_Contexts).split(",")) ;
			logEvent(args, "scavage", "srchSpecs: {}", srchSpecs) ;

			for ( final String _srchSpec : srchSpecs ) {
				Console.info("    Scavaging contexts @{} ...", _srchSpec) ;

				contextsWorkbooks = scavageSearchPath(_srchSpec, "xlsx", contextsWorkbooks) ;
				contextsTiddlyWikis = scavageSearchPath(_srchSpec, "html", contextsTiddlyWikis) ;
			}

			Console.info("    Scavaged Workbooks: {}, TiddlyWikis: {}", contextsWorkbooks.size(), contextsTiddlyWikis.size()) ;
		}


		return this ;
	}


	List<Path> scavageSearchPath(final String srchSpec, final String srchExtSpec, final List<Path> scavaged) throws IOException {

		final Path srchPath = Paths.get(FilenameUtils.getFullPath(srchSpec)) ;
		final String srchFSpec = FilenameUtils.getBaseName(srchSpec) + "." + srchExtSpec ;
		logEvent(args, "scavage", "scavage: {}/{}", srchPath, srchFSpec) ;


		scavaged.addAll(scavageFiles(srchPath, srchFSpec)) ;


		return scavaged ;
	}


	List<Path> scavageFiles(final Path srchPath, final String srchFSpec) throws IOException {

		List<Path> files ;

		if ( srchFSpec.contains("**") ) {

			final String globPattern = "glob:" + srchFSpec ;
			final PathMatcher globMatcher = FileSystems.getDefault().getPathMatcher(globPattern) ;

			try ( Stream<Path> stream = Files.walk(srchPath) ) {
				files = stream.filter(Files::isRegularFile).filter(globMatcher::matches).collect(Collectors.toList()) ;
				logEvent(args, "scavage", "glob: {}/{} -> {}", srchPath, srchFSpec, files) ;
			}
		}
		else
			try ( DirectoryStream<Path> stream = Files.newDirectoryStream(srchPath, srchFSpec) ) {
				files = StreamSupport.stream(stream.spliterator(), false).toList() ;
				logEvent(args, "scavage", "non-glob: {}/{} -> {}", srchPath, srchFSpec, files) ;
			}


		return files ;
	}


	//
	//
	//

	TWT write() throws IOException {

		Console.info("\nWriting ...") ;

		write(twDocument, decodedArgs, props) ;


		return this ;
	}


	TWT write(final StringBuilder tw, final Map<String, Object> cmdArgs, final Properties props) throws IOException {

		final String fSpec = (String) decodedArgs.get($ArgLong_TiddlyWiki) ;
		Logger.debug("TiddlyWiki: |{}|", fSpec) ;

		Console.info("    TiddlyWiki @{}", fSpec) ;

		FileUtils.writeStringToFile(new File(fSpec), tw.toString(), StandardCharsets.UTF_8) ;


		return this ;
	}


	//
	//
	//

	String replaceCmdTags(final String fSpec, final Map<String, Object> cmdArgs) {
		return replaceCmdTags(fSpec, cmdArgs, cmdTaggedTemplate) ;
	}


	String replaceCmdTags(final String fSpec, final Map<String, Object> cmdArgs, final Templator$Delimited tt) {

		String results = fSpec ;

		while ( results.indexOf(props.getProperty($Prop_TagOpenDelimiter)) != -1 )
			results = tt.template(results).inject(cmdArgs) ;


		return results ;
	}


	//
	//
	//

	public static void main(final String[] args)
			throws ParseException, FileNotFoundException, IOException, EncryptedDocumentException, ParserConfigurationException, SAXException {

		// Load properties ...

		Properties $props = Props.load(TWT.class, AppName + ".properties") ;
		CLArgs.loadAppProps($props, AppClassname, AppDesc, AppName, AppSee, AppUsage, AppVersion) ;


		// Parse and decode base command line arguments ...

		final CommandLine $cmd = CLArgs.parseCommandLine(args, CommandLineOptions, $props, ExitOnHelpOrVersion, Console) ;

		final TWT$Args $args = new TWT$Args($cmd, $props, args, Console) ;
		final Map<String, Object> $decodedArgs = $args.decodeCommandLine($args, CommandLineOptions, $props, ExitOnHelpOrVersion, Console) ;

		final String propFSpec = $decodedArgs.get($ArgLong_Base) + PathSeparator + $props.get($Prop_TiddlyWikiPropertiesFilename) ;
		$props = Props.merge($props, DoNotPropagateException, propFSpec) ;


		// Decode misc. arguments ...

		$decodedArgs.put($ArgLong_Provision, $args.decodeMiscArg_provision($cmd, $decodedArgs, $props, Console)) ;


		logEvent($args, "props", "props: {}", $props) ;
		logEvent($args, "args", "options: {}", Arrays.asList($cmd.getOptions())) ;
		logEvent($args, "args", "decodedArgs: {}", $decodedArgs) ;


		// Instantiate and process ...

		final TWT $ = new TWT($args, $decodedArgs, $props) ;

		CLArgs.printToolUsage(AppUsage, AppDesc, $cmd.getOptions(), Console) ;


		//

		$.scavage().archive().generate().extract().copy().assemble().write().provision() ;


		Console.info("\nDone.") ;
	}
}
