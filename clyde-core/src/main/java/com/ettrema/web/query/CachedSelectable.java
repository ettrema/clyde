package com.ettrema.web.query;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Folder;
import com.ettrema.web.RenderContext;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.eval.Evaluatable;
import com.ettrema.vfs.EmptyDataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;

/**
 * Currently not tested!!!
 * 
 * Will generate a query result once and persist it. Transaction is not commited
 * so must be done externally.
 * 
 * 
 *
 * @author bradm
 */
public class CachedSelectable implements Selectable,Evaluatable, Serializable {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CachedSelectable.class);
	private Selectable query;

	@Override
	public List<String> getFieldNames() {
		return query.getFieldNames();
	}

	@Override
	public List<FieldSource> getRows(Folder from) {
		log.trace("getRows");
		List<FieldSource> rows = getCachedRows(from);
		if (rows == null) {
			rows = query.getRows(from);
			setCachedRows(rows, from);
		}
		return rows;
	}
	

	@Override
	public long processRows(Folder from, RowProcessor rowProcessor) {
		long count = 0;
		for( FieldSource row : getRows(from)) {
			count++;
			rowProcessor.process(row);
		}
		return count;
	}	

	private List<FieldSource> getCachedRows(Folder from) {
		NameNode cachedNode = from.getNameNode().child("_sys_cachedquery");
		if (cachedNode == null) {
			log.trace("getCachedRows: no cached data");
			return null;
		}
		InputStream in = cachedNode.getBinaryContent();
		ObjectInputStream oin = null;
		List<FieldSource> cachedList = null;
		try {
			oin = new ObjectInputStream(in);
			cachedList = (List<FieldSource>) oin.readObject();
			log.trace("getCachedRows: returning cached rows: " + cachedList.size());
			return cachedList;
		} catch (IOException ex) {
			log.error("name node:" + cachedNode.getId(), ex);
			return null;
		} catch (ClassNotFoundException ex) {
			log.error("name node:" + cachedNode.getId(), ex);
			return null;
		} finally {
			IOUtils.closeQuietly(oin);
			IOUtils.closeQuietly(in);
		}
	}

	private void setCachedRows(final List<FieldSource> rows, Folder from) {
		NameNode cachedNode = from.getNameNode().child("_sys_cachedquery");
		if (cachedNode == null) {
			log.trace("setCachedRows: create node");
			cachedNode = from.getNameNode().add("_sys_cachedquery", new EmptyDataNode());
		}
		cachedNode.writeToBinaryOutputStream(new OutputStreamWriter<Long>() {

			@Override
			public Long writeTo(OutputStream out) {
				CountingOutputStream count = new CountingOutputStream(out);
				ObjectOutputStream oos = null;
				try {
					oos = new ObjectOutputStream(count);
					oos.writeObject(rows);
					count.flush();
					long bytes = count.getByteCount();
					log.trace("setCachedRows: wrote cache data: " + bytes + " bytes");
					return bytes;
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				} finally {
					IOUtils.closeQuietly(oos);
					IOUtils.closeQuietly(count);
				}
			}
		});

	}

	@Override
    public Object evaluate(RenderContext rc, Addressable from) {
		log.info("evaluate query");
        CommonTemplated relativeTo = (CommonTemplated) from;
        if (!(relativeTo instanceof Folder)) {
            relativeTo = relativeTo.getParentFolder();

        }
		List<FieldSource> list = getRows((Folder) relativeTo);
		log.trace("evaluate returned rows: " + list.size());
		return list;
    }

	@Override
    public Object evaluate(Object from) {
        Folder relativeTo = (Folder) from;
        List<FieldSource> list =  getRows(relativeTo);
		log.trace("evaluate returned rows: " + list.size());
		return list;
    }

	@Override
	public void pleaseImplementSerializable() {
		
	}

}
