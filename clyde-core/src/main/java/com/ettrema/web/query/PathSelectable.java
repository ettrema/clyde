package com.ettrema.web.query;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Folder;
import com.ettrema.web.ResourceQueryProcessor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public class PathSelectable implements Selectable, Serializable {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PathSelectable.class);
	private static final long serialVersionUID = 1L;
	private Path path;

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	@Override
	public List<String> getFieldNames() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<FieldSource> getRows(Folder from) {
		if (log.isTraceEnabled()) {
			log.trace("getRows: from: " + from.getHref() + " path: " + path);
		}
		final List<FieldSource> rows = new ArrayList<FieldSource>();
		ResourceQueryProcessor resourceQueryProcessor = new ResourceQueryProcessor();
		resourceQueryProcessor.find(path, from, new ResourceQueryProcessor.ResourceConsumer() {

			@Override
			public void onResource(Resource res) {
//                log.trace("matched resource: " + res.getName());
				if (res instanceof CommonTemplated) {
					CommonTemplated tres = (CommonTemplated) res;
					rows.add(new CommonTemplatedFieldSource(tres));
				}

			}
		});
		return rows;
	}

	@Override
	public long processRows(Folder from, final RowProcessor rowProcessor) {
		if (log.isTraceEnabled()) {
			log.trace("getRows: from: " + from.getHref() + " path: " + path);
		}

		ResourceQueryProcessor resourceQueryProcessor = new ResourceQueryProcessor();
		long count = resourceQueryProcessor.find(path, from, new ResourceQueryProcessor.ResourceConsumer() {

			@Override
			public void onResource(Resource res) {
//                log.trace("matched resource: " + res.getName());
				if (res instanceof CommonTemplated) {
					CommonTemplated tres = (CommonTemplated) res;
					rowProcessor.process(new CommonTemplatedFieldSource(tres));
				}

			}
		});
		return count;
	}

	public class CommonTemplatedFieldSource implements FieldSource {

		private final CommonTemplated resource;

		public CommonTemplatedFieldSource(CommonTemplated tres) {
			this.resource = tres;
		}

		@Override
		public Object get(String name) {
			Object o = resource.getValues().get(name);
			return o;
		}

		public CommonTemplated getResource() {
			return resource;
		}

		@Override
		public Object getData() {
			return resource;
		}

		@Override
		public Set<String> getKeys() {
			return resource.getTemplate().getComponentDefs().keySet();
		}
	}
}
