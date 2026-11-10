

package xcom.tiddly.wiki.misc ;


import static xcom.utils4j.CLArgs.logEvent ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;
import java.util.function.Predicate ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import xcom.tiddly.wiki.args.TWT$Args ;
import xcom.utils4j.data.structured.map.Maps ;

import static xcom.tiddly.wiki.misc.TiddlerFilter.CopyContextTypes.* ;


public class TiddlerFilter {

	public enum CopyContextTypes {
		CopyAllContext, CopyAllTaggedContext, CopyTaggedContextOnly, CopyFilteredContextOnly,;
	}


	public static final String QueryRegExPattern = "\\((.*?),(.*?)\\)" ;
	public static final String CopySelectorRegExPattern = ".*? twt-copy-selector=\"\\((.*?),[ ]*(.*?)\\)\".*?>" ;


	TWT$Args args ;

	/**
	 *
	 */
	List<Entry<String, String>> query ;

	public List<Entry<String, String>> getQuery() {
		return query ;
	}

	public TiddlerFilter setQuery(final List<Entry<String, String>> query) {
		this.query = query ;
		return this ;
	}


	/**
	 * @param args
	 * @param query
	 */
	public TiddlerFilter(final TWT$Args $args, final String query) {

		args = $args ;

		this.query = buildQuery(query) ;
	}


	/**
	 * @param query
	 * @return
	 */
	public static List<Entry<String, String>> buildQuery(final String query) {

		final List<Entry<String, String>> results = new ArrayList<>() ;

		final Matcher matcher = Pattern.compile(QueryRegExPattern, /* Pattern.DOTALL | */ Pattern.CASE_INSENSITIVE).matcher(query) ;

		while ( matcher.find() )
			results.add(Maps.getInstanceOfEntry(matcher.group(1), matcher.group(2))) ;


		return results ;
	}


	/**
	 * @param headers
	 * @param values
	 * @return
	 */
	public boolean matchesWorkbookValue(final Map<String, Short> headers, final Map<String, Object> values) {

		logEvent(args, "filter", "headers: {}", headers) ;
		logEvent(args, "filter", "values: {}", values) ;
		logEvent(args, "filter", "query: {}", query) ;


		if ( (query.size() == 0) || !query.stream().anyMatch(new Predicate<Entry<String, String>>() {
			@Override
			public boolean test(final Entry<String, String> entry) {
				return headers.containsKey(entry.getKey().startsWith("@") ? entry.getKey().substring(1) : entry.getKey()) ;
			}
		}) )
			return true ;


		for ( final Entry<String, String> _filter : query ) {

			final String _field ;
			final String _regex ;
			final String _value ;

			if ( _filter.getKey().startsWith("@") ) {

				_field = _filter.getKey().substring(1) ;
				_regex = "^" + (String) values.get(_filter.getKey().substring(1)) + "$" ;
				_value = _filter.getValue() ;
			}
			else {
				_field = _filter.getKey() ;
				_regex = "^" + _filter.getValue() + "$" ;
				_value = (String) values.get(_filter.getKey()) ;
			}

			logEvent(args, "filter", "criterion: {}, field: {}, regex-pattern: {}, match-value: {}", //
					_filter, _field, _regex, _value) ;

			if ( headers.containsKey(_field) && Pattern.compile(_regex, Pattern.CASE_INSENSITIVE).matcher(_value).find() ) {

				logEvent(args, "filter", "matched") ;
				return true ;
			}

			logEvent(args, "filter", "not matched") ;
		}


		return false ;
	}


	/**
	 * @param tiddler
	 * @param copyType
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public boolean matchesCopyContextType(final StringBuilder tiddler, CopyContextTypes copyType) {

		logEvent(args, "filter", "mode: {}, tiddler-regex: {}, query: {}", copyType, CopySelectorRegExPattern, query) ;


		// CopyAllContext, CopyAllTaggedContext, CopyTaggedContextOnly, CopyFilteredContextOnl,

		if ( copyType == CopyAllContext )
			return true ;

		if ( copyType == CopyFilteredContextOnly && query.size() == 0 )
			return false ;


		final Pattern pattern = Pattern.compile(CopySelectorRegExPattern, Pattern.CASE_INSENSITIVE) ;
		final Matcher matcher = pattern.matcher(tiddler) ;
		boolean selectorFound = matcher.find() ;

		if ( selectorFound )
			logEvent(args, "filter", "field: {}, regex-pattern: {}", matcher.group(1), matcher.group(2)) ;


		if ( copyType == CopyAllTaggedContext && selectorFound )
			return true ;

		if ( copyType == CopyFilteredContextOnly && !selectorFound )
			return false ;

		if ( copyType == CopyTaggedContextOnly && selectorFound )
			return false ;

		if ( copyType == CopyTaggedContextOnly && !selectorFound )
			return true ;


		for ( final Entry<String, String> _filter : query ) {

			final String _regex = "^" + matcher.group(2) + "$" ;

			logEvent(args, "filter", "criterion: {}, field: {}, regex-pattern: {}, match-value: {}", //
					_filter, _filter.getKey(), _regex, _filter.getValue()) ;

			if ( _filter.getKey().equals(matcher.group(1)) //
					&& Pattern.compile(_regex, Pattern.CASE_INSENSITIVE).matcher(_filter.getValue()).find() ) {

				logEvent(args, "filter", "matched") ;
				return true ;
			}

			logEvent(args, "filter", "not matched") ;
		}


		return false ;
	}
}
