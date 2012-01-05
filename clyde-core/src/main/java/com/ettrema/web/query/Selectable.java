package com.ettrema.web.query;

import com.ettrema.web.Folder;
import java.util.List;

/**
 *
 * @author brad
 */
public interface Selectable {
    List<String> getFieldNames();
    List<FieldSource> getRows(Folder from);
	
	/**
	 * Execute the query processing rows as it goes. Every attempt is made to avoid
	 * buffering data in memory
	 * 
	 * @param from
	 * @param rowProcessor
	 * @return - number of rows processed
	 */
	long processRows(Folder from, RowProcessor rowProcessor);
	
	public interface RowProcessor {
		void process(FieldSource row);
	}
}
