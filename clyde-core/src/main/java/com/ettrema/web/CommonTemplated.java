package com.bradmcevoy.web;

import com.ettrema.utils.GroovyUtils;
import java.util.HashMap;
import com.bradmcevoy.web.component.Command;
import com.bradmcevoy.web.component.ComponentUtils;
import com.ettrema.event.ClydeEventDispatcher;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.utils.HrefService;
import com.bradmcevoy.utils.RedirectService;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.NumberInput;
import com.bradmcevoy.web.component.TemplateSelect;
import com.bradmcevoy.web.error.HtmlExceptionFormatter;
import com.bradmcevoy.web.eval.EvalUtils;
import com.bradmcevoy.web.eval.Evaluatable;
import com.bradmcevoy.web.search.FolderSearcher;
import com.bradmcevoy.web.security.ClydeAuthenticator;
import com.bradmcevoy.web.security.ClydeAuthoriser;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jdom.Element;


import static com.ettrema.context.RequestContext._;

public abstract class CommonTemplated extends VfsCommon implements PostableResource, GetableResource, EditableResource, Addressable, Serializable, ComponentContainer, Comparable<Resource>, Templatable, HtmlResource, DigestResource, PropFindableResource {

	public static final String MAXAGE_COMP_NAME = "maxAge";
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommonTemplated.class);
	private static final long serialVersionUID = 1L;
	private static ThreadLocal<CommonTemplated> tlTargetPage = new ThreadLocal<CommonTemplated>();
	public static ThreadLocal<BaseResource> tlTargetContainer = new ThreadLocal<BaseResource>();
	protected TemplateSelect templateSelect;
	protected ComponentValueMap valueMap;
	protected ComponentMap componentMap;
	/**
	 * Groovy script executed when a POST is done to this page
	 */
	private String onPostScript;
	private transient Params params;
	/**
	 * If not null will overide the default of text/html
	 */
	private String contentType;

	/**
	 * 
	 * @return - the page from which the last component was found on this thread - EEK!
	 */
	public static BaseResource getTargetContainer() {
		return tlTargetContainer.get();
	}

	public static void clearThrealocals() {
		tlTargetPage.remove();
		tlTargetContainer.remove();
	}

	@Override
	public abstract String getName();

	@Override
	public abstract Templatable getParent();

	/**
	 * Called only when the persisted content type is null, this should be
	 * what we expect the content type of a resource type to be, possibly
	 * considering its file extension
	 *
	 * @return
	 */
	public abstract String getDefaultContentType();

	public CommonTemplated() {
		valueMap = new ComponentValueMap();
		componentMap = new ComponentMap();
		templateSelect = new TemplateSelect(this, "template");
		componentMap.add(templateSelect);
	}

	/**
	 * Find a resource from the given path. If relative, the search is done from this
	 * resource. If absolute, the search is from the host
	 * 
	 * @param path
	 * @return
	 */
	@Override
	public Templatable find(Path path) {
		return ComponentUtils.find(this, path);
	}

	public Templatable find(String sPath) {
		Path p = Path.path(sPath);
		return find(p);
	}

	public BaseResourceList search(String sPath) {
		Path p = Path.path(sPath);
		return search(p);
	}

	private BaseResourceList search(Path p) {
		return FolderSearcher.getFolderSearcher().search(this, p);
	}

	public String getTitle() {
		String s = getTitleNoName();
		if (s == null || s.trim().length() == 0 ) {
			s = this.getName();
		}
		return s;
	}

	public String getTitleNoName() {
		ComponentValue cv = this.getValues().get("title");
		if (cv != null) {
			Object o = cv.getValue();
			if (o != null) {
				return o.toString();
			}
		}
		Component c = this.getComponent("title", false);
		if (c != null) {
			String s = c.render(new RenderContext(getTemplate(), this, null, false));
			return s;
		}
		return null;
	}

	public String getBrief() {
		ComponentValue cv = this.getValues().get("brief");
		if (cv != null) {
			Object o = cv.getValue();
			if (o != null) {
				return o.toString();
			}
		}
		return getTitle();
	}

	@Override
	public Addressable getContainer() {
		return getParent();
	}

	@Override
	public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
		log.info("processForm");
		String redirect = doOnPost(parameters, files);
		if (redirect != null) {
			return redirect;
		}
		preProcess(null, parameters, files);
		String s = process(null, parameters, files);
		return s;
	}

	/** Components should read their values from request params
	 */
	@Override
	public void preProcess(RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files) {
		ITemplate lTemplate = getTemplate();
		RenderContext rc = new RenderContext(lTemplate, this, rcChild, false);
		if (lTemplate != null) {
			lTemplate.preProcess(rc, parameters, files);
			for (ComponentDef def : lTemplate.getComponentDefs().values()) {
				ComponentValue cv = this.getValues().get(def.getName());
				if (cv == null) {
					cv = def.createComponentValue(this);
					getValues().add(cv);
				}
				def.onPreProcess(cv, rc, parameters, files);
			}
		}

		for (String paramName : parameters.keySet()) {
			Path path = Path.path(paramName);
			Component c = rc.findComponent(path);
			if (c != null) {
				c.onPreProcess(rc, parameters, files);
			}
		}
	}

	/** Commands should be invoked, if user clicked
	 */
	@Override
	public String process(RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
		log.info("process");
		ITemplate lTemplate = getTemplate();
		RenderContext rc = new RenderContext(lTemplate, this, rcChild, false);
		String redirectTo = null;

		for (String paramName : parameters.keySet()) {
			Path path = Path.path(paramName);
			Component c = rc.findComponent(path);
			if (c != null) {
				log.info("-- processing command: " + c.getClass().getName() + " - " + c.getName());
				redirectTo = c.onProcess(rc, parameters, files);
				if (redirectTo != null) {
					log.trace(".. redirecting to: " + redirectTo);
					return redirectTo;
				}
			}
		}
		return null;
	}

	/**
	 *  Must be absolute
	 * @return
	 */
	@Override
	public String getHref() {
		return _(HrefService.class).getHref(this);
	}

	/**
	 * 
	 * @return - the absolute path of this resource. does not include server
	 */
	@Override
	public String getUrl() {
		return _(HrefService.class).getUrl(this);
	}

	@Override
	public Path getPath() {
		Templatable lParent = getParent();
		if (lParent == null) {
			return Path.root();
		}
		Path p = lParent.getPath();
		p = p.child(getName());
		return p;
	}

	@Override
	public Folder getParentFolder() {
		return Folder.find(this);
	}

	@Override
	public Web getWeb() {
		return Web.find(this);
	}

	/**
	 * 
	 * @return - size in bytes of persisted components and component values
	 */
	public long getPersistedSize() {
		long size = 100;
		for (ComponentValue cv : this.getValues().values()) {
			Object val = cv.getValue();
			if (val == null) {
			} else if (val instanceof String) {
				size += ((String) val).length();
			} else {
				size += 100; // approx
			}
		}
		for (Component c : this.getComponents().values()) {
			size += 100;
		}
		return size;
	}

	@Override
	public Collection<Component> allComponents() {
		return ComponentUtils.allComponents(this);
	}

	@Override
	public Params getParams() {
		if (params == null) {
			params = new Params();
		}
		return params;
	}

	/**
	 * An alias for getParams, this is to allow a consistent templating syntax:
	 *
	 * $file.view.title
	 * ...and...
	 * $view.title
	 */
	public Params getView() {
		return getParams();
	}

	@Override
	public boolean is(String type) {
		ITemplate t = getTemplate();
		return (t != null) && t.represents(type);
	}

	@Override
	public PostableResource getEditPage() {
		return new EditPage(this);
	}

	public void loadFromXml(Element el) {
		// if not present, just ignore values (eg for code behind page)
		if (el.getChild("componentValues") != null) {
			getValues().fromXml(el, this);
		}
		getComponents().fromXml(this, el);
		templateSelect = (TemplateSelect) componentMap.get("template");
		this.contentType = InitUtils.getValue(el, "contentType");
		if (templateSelect == null) {
			templateSelect = new TemplateSelect(this, "template");
			componentMap.add(templateSelect);
			String s = InitUtils.getValue(el, "template");
			templateSelect.setValue(s);
		}
		Element elScript = el.getChild("onPostScript");
		log.trace("loadFromXml: " + el.getName() + elScript);
		if (elScript != null) {
			onPostScript = elScript.getText();
			log.trace("loadFromXml: onPostPageScript: " + onPostScript);
		}

	}

	@Override
	public IUser authenticate(String user, String password) {
		ClydeAuthenticator authenticator = requestContext().get(ClydeAuthenticator.class);
		IUser o = authenticator.authenticate(this, user, password);
		if (o == null) {
			log.warn("authentication failed by: " + authenticator.getClass());
		}
		return o;
	}

	@Override
	public Object authenticate(DigestResponse digestRequest) {
		ClydeAuthenticator authenticator = requestContext().get(ClydeAuthenticator.class);
		Object o = authenticator.authenticate(this, digestRequest);
		if (o == null) {
			log.warn("authentication failed by: " + authenticator.getClass());
		}
		return o;
	}

	@Override
	public boolean isDigestAllowed() {
		return true;
	}

	public Host findHost(String authority) {
		Host h = getHost();
		if (authority == null) {
			return h;
		}
		while (h != null && !h.getName().equals(authority)) {
			h = h.getParentHost();
		}
		return h;
	}

	public Host getParentHost() {
		Folder f = getParentFolder();
		if (f == null) {
			return null;
		}
		return f.getHost();
	}

	@Override
	public boolean authorise(Request request, Request.Method method, Auth auth) {
		log.trace("start authoirse");
		try {
			ClydeAuthoriser authoriser = requestContext().get(ClydeAuthoriser.class);
			boolean b = authoriser.authorise(this, request, method, auth);
			return b;
		} finally {
			log.trace("finished authorise");
		}
	}

	@Override
	public String checkRedirect(Request request) {
		String s = _(RedirectService.class).checkRedirect(this, request);
		return s;
	}

	@Override
	public int compareTo(Resource o) {
		Resource res = o;
		return this.getName().toUpperCase().compareTo(res.getName().toUpperCase()); // todo: this will be unstable. should fall back to case sensitive if both names are otherwise equal
	}

	@Override
	public ComponentMap getComponents() {
		if (componentMap == null) {
			componentMap = new ComponentMap();
		}
		return componentMap;
	}

	@Override
	public Long getContentLength() {
		return null;
	}

	@Override
	public String getContentType(String accepts) {
		String ct;
		if (this.contentType != null && contentType.length() > 0) {
			ct = contentType;
		} else {
			//ct = null;
			ct = getDefaultContentType();
		}
		if (ct != null && ct.contains(",")) {
			String[] arr = ct.split(",");
			ct = arr[0];
		}

		if (log.isTraceEnabled()) {
			log.trace("getContentType: " + accepts + " -> " + ct);
		}
		return ct;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Gets the max age configuration on this instance (non-recursive)
	 * 
	 * @return
	 */
	public Long getMaxAgeSecsThis() {
		Component c = this.getComponent(MAXAGE_COMP_NAME);
		if (c == null) {
			return null;
		}
		return getMaxAgeSecs(c);
	}

	public void setMaxAgeSecsThis(Long l) {
		if (l == null) {
			setMaxAgeSecsThis((Integer) null);
		} else {
			setMaxAgeSecsThis((int) l.longValue());
		}
	}

	public void setMaxAgeSecsThis(Integer l) {
		if (l == null) {
			this.getComponents().remove(MAXAGE_COMP_NAME);
		} else {
			Component c = this.getComponents().get(MAXAGE_COMP_NAME);
			NumberInput n = null;
			if (c instanceof NumberInput) {
				n = (NumberInput) c;
			}

			if (n == null) {
				n = new NumberInput(this, MAXAGE_COMP_NAME);
				this.getComponents().add(n);
			}
			if (l == null) {
				n.setValue(null);
			} else {
				n.setValue(l.intValue());
			}
		}
	}

	private Long getMaxAgeSecs(Component c) {
		if (c instanceof NumberInput) {
			NumberInput n = (NumberInput) c;
			Integer ii = n.getValue();
			if (log.isTraceEnabled()) {
				log.trace("using maxAge component from: " + c.getContainer().getPath() + " = " + ii);
			}
			if (ii == null) {
				return null;
			}
			return (long) ii.intValue();
		} else {
			if (log.isTraceEnabled()) {
				log.trace("maxAge component is not compatible type: " + c.getClass() + " from: " + c.getContainer().getPath());
			}
			return null;
		}

	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		Component c = this.getComponent("maxAge");
		if (c != null) {
			Long l = getMaxAgeSecs(c);
			if (l != null) {
				if (log.isTraceEnabled()) {
					log.trace("found a maxAge component with value: " + l);
				}
				return l;
			} else {
				log.trace("maxAge component has null value so ignore it and use default");
			}
		}
		if (this.getTemplate() == null) {
			log.trace("no template so probably not a templated rsource so use large default max-age");
			return 315360000l;
		} else {
			log.trace("get default max age");
			return getDefaultMaxAge(auth);
		}
	}

	protected long getDefaultMaxAge(Auth auth) {
		if (auth == null) {
			//log.trace( "no authentication, use long max-age" );
			return 60 * 60 * 24l;
		} else {
			//log.trace( "authenticated, use short max-age" );
			return 60l;
		}
	}

	@Override
	public ITemplate getTemplate() {
		ITemplate template = null;
		Web web = getWeb();
		if (web != null) {
			String templateName = getTemplateName();
			if (templateName == null || templateName.length() == 0 || templateName.equals("null")) {
				log.debug("empty template name");
				return null;
			}
			TemplateManager tm = requestContext().get(TemplateManager.class);
			template = tm.lookup(templateName, web);
			if (template == null) {
				log.warn("no template: " + templateName + " for web: " + web.getName());
			} else {
				if (template == this) {
					throw new RuntimeException("my template is myself");
				}
			}
		} else {
			log.warn("no web for: " + this.getName());
		}
//        if( template != null ) {
//            log.debug( "end: getTemplate: from:" + this.getName() + " template:" + getTemplateName() + " -->> " + template.getClass() + ": " + template.getName());
//        }
		return template;
	}

	@Override
	public String getTemplateName() {
		TemplateSelect sel = getTemplateSelect();
		if (sel == null) {
			log.trace("getTemplateName: no template component`");
			return null;
		}
		String s = sel.getValue();
		log.trace("templatename: " + s + " - " + getClass());
		return s;
	}

	public TemplateSelect getTemplateSelect() {
		TemplateSelect sel = (TemplateSelect) getComponents().get("template");
		if (sel == null) {
			sel = new TemplateSelect(this, "template");
			templateSelect = sel;
			componentMap.add(templateSelect);
		}
		return sel;
	}

	@Override
	public ComponentValueMap getValues() {
		if (valueMap == null) {
			valueMap = new ComponentValueMap();
		}
		return valueMap;
	}

	public String render(RenderContext child) {
		_(ClydeEventDispatcher.class).beforeRender(this, child);
		ITemplate t = getTemplate();
		if (t == null) {
//            log.debug( "render: null template for: " + this.getName());
		}
		RenderContext rc = new RenderContext(t, this, child, false);
		if (t != null) {
//            log.debug( "render: rendering from template " + t.getName());
			return t.render(rc);
		} else {
//            log.debug( "render: no template, so try to use root parameter" );
			Component cRoot = this.getParams().get("root");
			if (cRoot == null) {
				log.warn("render: no template " + this.getTemplateName() + " and no root or body component for template: " + this.getHref());
				return "";
			} else {
//                log.debug( "render: rendering from root component");
				return cRoot.render(rc);
			}
		}
	}

	public String renderEdit(RenderContext rc) {
		return rc.doBody();
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
		generateContent(out);
	}

	public void generateContent(OutputStream out) throws IOException {
		if (log.isTraceEnabled()) {
			log.trace("sendContent: " + this.getHref());
		}
		//tlTargetPage.set(this);
		if (RequestParams.current() != null) {
			RequestParams.current().attributes.put("targetPage", this);
		}
		String s = null;
		try {
			s = render(null);
		} catch (Throwable e) {
			// TODO move to context
			HtmlExceptionFormatter formatter = new HtmlExceptionFormatter();
			s = formatter.formatExceptionAsHtml(e);
		}
		try {
			out.write(s.getBytes());
		} catch (Exception e) {
			log.error("Exception sending error page", e);
			throw new RuntimeException("Exception sending error page", e);
		}
	}

	public CommonTemplated getRequestPage() {
		//return tlTargetPage.get();
		return (CommonTemplated) RequestParams.current().attributes.get("targetPage");
	}

	public void setTemplate(Page template) {
		this.templateSelect.setValue(template.getName());
	}

	public void setTemplateName(String templateName) {
		this.templateSelect.setValue(templateName);
	}

	/**
	 * This supports components
	 * 
	 * @param container
	 * @param el
	 * @return
	 */
	public Element toXml(Addressable container, Element el) {
		log.warn("toXml");
		Element e2 = new Element("component");
		el.addContent(e2);
		populateXml(e2);
		return e2;
	}

	public void populateXml(Element e2) {
		log.trace("populateXml");
		e2.setAttribute("class", this.getClass().getName());
		getValues().toXml(this, e2);
		getComponents().toXml(this, e2);
		InitUtils.setString(e2, getTemplateSelect());
		InitUtils.setString(e2, "contentType", contentType);
		if (onPostScript != null) {
			Element elScript = new Element("onPostScript");
			elScript.setText(onPostScript);
			e2.addContent(elScript);
			log.trace("populateXml: onPostScript: " + onPostScript);
		}
	}

	public Object value(String name) {
		ComponentValue cv = valueMap.get(name);
		if (cv == null) {
			return null;
		}
		return cv.getValue();
	}

	public void setValue(String name, Object val) {
		ComponentValue cv = valueMap.get(name);
		ITemplate t = null;
		ComponentDef def = null;
		if (cv == null) {
			t = this.getTemplate();
			if (t != null) {
				def = t.getComponentDef(name);
			}
			if (def != null) {
				cv = def.createComponentValue(this);
			} else {
				cv = new ComponentValue(name, this);
			}
			valueMap.add(cv);
		}
		if (val instanceof String) {
			if (def == null) {
				if (t == null) {
					t = this.getTemplate();
					if (t != null) {
						def = t.getComponentDef(name);
						if (def != null) {
							val = def.parseValue(cv, t, (String) val);
						} else {
							log.warn("no component def found: " + name + " in template: " + t.getName());
						}
					}
				}
			}
		}
		cv.setValue(val);
	}

	@Override
	public Host getHost() {
		Web web = getWeb();
		if (web == null) {
			log.warn("null web for: " + this.getPath() + " - " + this.getName() + " - " + this.getClass());
			return null;
		}
		Host h = web.getHost();
		if (h == null) {
			log.warn("null host for: " + this.getPath());
		}
		return h;
	}

	/**
	 * 
	 * @param text
	 * @return - html to show a link to this file with the supplied text
	 */
	@Override
	public String link(String text) {
		return "<a href='" + getHref() + "'>" + text + "</a>";
	}

	@Override
	public String getLink() {
		String text = getLinkText();
		return link(text);
	}

	public String getLinkText() {
		String s = getTitle();
		if (s == null || s.length() == 0) {
			return getName();
		} else {
			return s;
		}
	}

	public Resource getChildResource(String childName) {
//        log.debug( "getChildResource: " + childName + " from: " + this.getHref());
		Component c = getAnyComponent(childName);
		//System.out.println("get component: " + childName + " - got " + c);
		Resource r = null;
		if (c != null) {
			// nasty hacks to ensure the physical resource is always available
			// to components from subpages and templates
			if (this instanceof BaseResource) {
//                log.debug( "setting target container: " + this.getHref());
				tlTargetContainer.set((BaseResource) this);
			} else {
//                log.debug( "not setting: " + this.getClass());
			}
		}
		if (c instanceof Resource) {
			r = (Resource) c;
		} else if (c instanceof ComponentValue) {
			ComponentValue cv = (ComponentValue) c;
			Object o = cv.getValue();
			if (o != null && (o instanceof Resource)) {
				r = (Resource) o;
			}
		}
		return r;
	}

	/**
	 * 
	 * @param childName
	 * @return - a component of any type which has the given name
	 */
	@Override
	public Component getAnyComponent(String name) {
		Component c;

		c = getValues().get(name);
		if (c != null) {
			return c;
		}

		c = getComponent(name);
		if (c != null) {
			return c;
		}

		return null;
	}

	/**
	 * find a component on this instance or any of its ancestor templates.
	 * If there are multiple components of the same name, the one closest
	 * to the final instance overrides inherited ones
	 *
	 * This will not return component definitions or values
	 *
	 * If the component is wrappable and is inherited it will be wrapped
	 *
	 * @param paramName
	 * @return
	 */
	public Component getComponent(String paramName) {
		return getComponent(paramName, false);
	}

	/**
	 * Recursively retrieves the component from this resource or any ancestor
	 * template
	 *
	 * @param paramName
	 * @param includeValues
	 * @return
	 */
	@Override
	public Component getComponent(String paramName, boolean includeValues) {
		log.debug("getComponent: " + paramName + " - " + this.getName());
		return ComponentUtils.getComponent(this, paramName, includeValues);
	}

	public BaseResourceList getParents() {
		BaseResourceList list = new BaseResourceList();
		Templatable t = this.getParent();
		while (t != null) {
			list.add(t);
			if (t instanceof Host) {
				t = null;
			} else {
				t = t.getParent();
			}
		}
		return list;
	}

	public Component _invoke(String name) {
		ComponentValue cv = this.getValues().get(name);
		if (cv != null) {
			return cv;
		}
		Component c = this.getComponents().get(name);
		if (c != null) {
			return c;
		}
		ITemplate t = getTemplate();
		if (t == null) {
			return null;
		}
		return t._invoke(name);
	}

	/**
	 *
	 * @param name
	 * @return - must never return null!
	 */
	public String invoke(String name) {
		Component c = _invoke(name);
		if (c == null) {
			return "";
		}
		RenderContext rc = new RenderContext(this.getTemplate(), this, null, false);
		return c.render(rc);
	}

	public String execute(String name) throws NotAuthorizedException {
		Component c = _invoke(name);
		if (c == null) {
			return "";
		} else if (c instanceof Command) {
			RenderContext rc = new RenderContext(this.getTemplate(), this, null, false);
			Command cmd = (Command) c;
			cmd.process(this);
			return c.render(rc);
		} else {
			throw new RuntimeException("Component is not a command");
		}
	}

	public String getFirstPara() {
		return firstPara("body");
	}

	public String firstPara(String paramName) {
		String s = invoke(paramName);
		int posEnd = s.indexOf("</p>");
		if (posEnd > 0) {
			int posStart = s.indexOf("<p>");
			if (posStart > 0) {
				posStart = posStart + 3; // for p tag
				posEnd = s.indexOf("</p>", posStart); // need to find first closing for this opening
				return s.substring(posStart, posEnd);
			}
		}
		return "";
	}

	public boolean hasValue(String name) {
		ComponentValue cv = valueMap.get(name);
		if (cv == null) {
			return false;
		} else {
			return !cv.isEmpty();
		}
	}

	public boolean isInTemplates() {
		Folder parent = this.getParentFolder();
		if (parent == null) {
			return false;
		} else {
			if (parent.getName().equals("templates")) {
				return true;
			} else {
				return parent.isInTemplates();
			}
		}

	}

	private String doOnPost(Map<String, String> parameters, Map<String, FileItem> files) {
		if (this.onPostScript != null) {
			log.trace("onAfterSave: run script");
			Map map = new HashMap();
			Object redirect = GroovyUtils.exec(this, map, onPostScript);
			if (redirect instanceof String) {
				return redirect.toString();
			}
		}
		ITemplate template = getTemplate();
		if (template != null) {
			template.onPost(this);
		}
		return null;
	}

	public class Params implements Map<String, Component> {

		@Override
		public int size() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isEmpty() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean containsKey(Object key) {
			if (key instanceof String) {
				Component c = get((String) key);
				return c != null;
			} else {
				return false;
			}
		}

		@Override
		public boolean containsValue(Object value) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Component get(Object key) {
			if (log.isTraceEnabled()) {
				log.trace("params.get: " + key);
			}
			String sKey = key.toString();
//            return getAnyComponentRecursive(sKey);
			Component c = getComponent(sKey, true);
			if (c != null) {
				log.trace("got a component");
				return c;
			}
			ITemplate template = getTemplate();
			if (template == null) {
				log.trace("template is null so can't return component");
				return null;
			}

			ComponentDef def = template.getComponentDef(sKey);
			if (def == null) {
				log.trace("no component found and no component def on the template: field:" + sKey + " template: " + template.getName());
				return null;
			}
			log.trace("creating a new component value");
			ComponentValue cv = def.createComponentValue(CommonTemplated.this);
			getValues().put(sKey, cv);
			return cv;
		}

		@Override
		public Component put(String key, Component value) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Component remove(Object key) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void putAll(Map<? extends String, ? extends Component> m) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void clear() {

			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Set<String> keySet() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Collection<Component> values() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Set<Entry<String, Component>> entrySet() {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

	public Object eval(String name) {
		Component c = this.getComponent(name);
		RenderContext rc = new RenderContext(getTemplate(), this, null, false);
		if (c instanceof Evaluatable) {
			Evaluatable eval = (Evaluatable) c;
			return EvalUtils.eval(eval, rc, this);
		} else {
			if (c == null) {
				return "";
			} else {
				return c.render(rc);
			}
		}
	}

	public String getOnPostScript() {
		return onPostScript;
	}

	public void setOnPostScript(String onPostScript) {
		this.onPostScript = onPostScript;
	}
}