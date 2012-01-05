package com.ettrema.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.ComponentContainer;
import com.ettrema.web.ExistingResourceFactory;
import com.ettrema.web.Host;
import com.ettrema.web.ITemplate;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

public class ComponentUtils {

    public static Path findPath(Component c) {
        Path p = findPath(c.getContainer());
        return p.child(c.getName());
    }

    /**
     * Build a path object for the given container. Implemented here because
     * it must be symmetrical with findComponent(Path)
     *
     * @param container
     * @return - a path from the host to the container
     */
    public static Path findPath(Addressable container) {
        Addressable parent = container.getContainer();
        Path parentPath;
        if (parent != null && !(parent instanceof Host)) {
            parentPath = findPath(parent);
        } else {
            parentPath = Path.root();
        }
        return parentPath.child(container.getName());
    }

    public static Templatable find(Templatable from, Path p) {
        Templatable ct;
        if (p == null) {
            throw new NullPointerException("path is null");
        }
        if (!p.isRelative()) {
            if (from == null) {
                throw new NullPointerException("from is null");
            }
            ct = findPageWithRelativePath(p, from.getWeb());
        } else {
            ct = findPageWithRelativePath(p, from);
        }
        return ct;
    }

    public static Templatable findPageWithRelativePath(Path path, Templatable page) {
        if (path == null) {
            return page;
        }
        Resource r = ExistingResourceFactory.findChild(page, path);
        if (r instanceof Templatable) {
            return (Templatable) r;
        }
        return null;
    }

    public static boolean validateComponents(Object target, RenderContext rc) {
        Map<String, Field> fields = InitUtils.componentFields(target);
        boolean b = true;
        for (Field f : fields.values()) {
            Component c = InitUtils.getComponent(f, target);
            b = b && c.validate(rc);
        }
        return b;
    }

    public static boolean validatePage(CommonTemplated page, RenderContext rc) {
        boolean ok = true;
        for (Component v : page.allComponents()) {
            if (v instanceof Command) {
                // ignore
            } else {
                ok = ok && v.validate(rc);
            }
        }
        return ok;
    }

    /**
     * Eg find a component called
     *
     * @param path
     * @param startFrom
     * @return
     */
    public static Component findComponent(Path path, Templatable startFrom) {
        if (!path.isRelative()) {
            startFrom = startFrom.getWeb();
        }
        ComponentContainer parent = findContainer(path.getParent(), startFrom);
        if (parent == null) {
            return null;
        } else {
            Component c = parent.getAnyComponent(path.getName());
            return c;
        }
    }

    public static ComponentContainer findContainer(Path path, Templatable from) {
        if (path == null || path.isRoot()) {
            return from;
        } else {
            ComponentContainer parent = findContainer(path.getParent(), from);
            if (parent == null) {
                return null;
            } else {
                if (parent instanceof Resource) {
                    Resource rParent = (Resource) parent;
                    Resource child = ExistingResourceFactory.findChild(rParent, path.getName());
                    if (child != null) {
                        if (child instanceof ComponentContainer) {
                            return (ComponentContainer) child;
                        } else {
                            return null;
                        }
                    }
                }
                Component c = parent.getAnyComponent(path.getName());
                if (c instanceof ComponentContainer) {
                    return (ComponentContainer) c;
                } else {
                    return null;
                }

            }

        }
    }

    public static List<Component> getAllComponents(Templatable page) {
        List<Component> list = new ArrayList<Component>();
        addComponents(page, list);
        return list;
    }

    private static void addComponents(Templatable page, List<Component> list) {
        if (page == null) {
            return;
        }
        for (Component c : page.getValues().values()) {
            list.add(c);
        }
        for (Component c : page.getComponents().values()) {
            list.add(c);
        }
        addComponents(page.getTemplate(), list);
    }

    public static Collection<Component> allComponents(Templatable res) {
        Map<String, Component> map = new HashMap<String, Component>();
        ITemplate parentTemplate = res.getTemplate();
        if (parentTemplate != null) {
            addInheritedComponents(parentTemplate, map);
        }
        for (ComponentValue cv : res.getValues().values()) {
            map.put(cv.getName(), cv);
        }
        for (Component c : res.getComponents().values()) {
            map.put(c.getName(), c);
        }
        Set set = new HashSet(map.values());
        List<Component> list = new ArrayList<Component>();
        list.addAll(set);
        Collections.sort(list, new com.ettrema.web.ComponentComparator());
        return list;
    }

    private static void addInheritedComponents(Templatable res, Map<String, Component> map) {
        if (res == null) {
            return;
        }
        ITemplate p = res.getTemplate();
        addInheritedComponents(p, map);
        for (Component c : res.getComponents().values()) {
            map.put(c.getName(), c);
        }
    }

    /**
     * Recursively looks for a component (or ComponentValue if includeValues is true)
     * from the given resource and templates
     *
     * @param res
     * @param paramName
     * @param includeValues
     * @return
     */
    public static Component getComponent(Templatable res, String paramName, boolean includeValues) {
        Component c;
        if (includeValues) {
            c = res.getValues().get(paramName);
            if (c != null) {
                return c;
            }
        }
        c = res.getComponents().get(paramName);
        if (c != null) {
            return c;
        }

        ITemplate t = res.getTemplate();
        if (t == null) {
            return null;
        }

        c = getComponent(t, paramName, includeValues);
        if (c != null) {
            if (c instanceof WrappableComponent) {
                return new WrappedComponent(res, (WrappableComponent) c);
            } else {
                return c;
            }
        } else {
            return null;
        }
    }

    public static boolean isEmpty(Object val) {
        if (val == null) {
            return true;
        } else if (val instanceof String) {
            String s = (String) val;
            return StringUtils.isBlank(s);
        } else {
            return false;
        }
    }

    /**
     * TODO: replace with JTidy or AntiSamy
     * http://jtidy.sourceforge.net/multiproject/jtidyservlet/clover/org/w3c/tidy/servlet/util/HTMLEncode.html
     * 
     *
     * @param s
     * @return
     */
    public static String encodeHTML(String s) {
//        s = s.replace("& ", "&amp; ");
//        StringBuilder out = new StringBuilder();
//        for (int i = 0; i < s.length(); i++) {
//            char c = s.charAt(i);
//            if (c > 127 || c == '"' || c == '<' || c == '>') {
//                out.append("&#" + (int) c + ";");
//            } else {
//                out.append(c);
//            }
//        }
//        return out.toString();
        return encode(s, "\n");
    }
    private static HashMap<String, String> entityTableEncode = null;
    private static final String[] ENTITIES = {
        ">",
        "&gt;",
        "<",
        "&lt;",
        "&",
        "&amp;",
        "\"",
        "&quot;",
        "'",
        "&#039;",
        "\\",
        "&#092;",
        "\u00a9",
        "&copy;",
        "\u00ae",
        "&reg;"};

    protected static synchronized void buildEntityTables() {
        entityTableEncode = new HashMap<String, String>(ENTITIES.length);

        for (int i = 0; i < ENTITIES.length; i += 2) {
            if (!entityTableEncode.containsKey(ENTITIES[i])) {
                entityTableEncode.put(ENTITIES[i], ENTITIES[i + 1]);
            }
        }
    }

    public static String encode(String s, String cr) {
        if (entityTableEncode == null) {
            buildEntityTables();
        }
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() * 2);
        char ch;
        for (int i = 0; i < s.length(); ++i) {
            ch = s.charAt(i);
            if ((ch >= 48 && ch <= 59) || (ch >= 63 && ch <= 90) || (ch >= 97 && ch <= 122) || (ch == ' ')) {
                sb.append(ch);
            } else if (ch == '\n') {
                sb.append(cr);
            } else {
                // cherry pick some safe non-seq chars
                if (ch == '(' || ch == ')' || ch == '+' || ch == '-' || ch == '*' || ch == '_') {
                    sb.append(cr);
                } else {
                    String chEnc = encodeSingleChar(String.valueOf(ch));
                    if (chEnc != null) {
                        sb.append(chEnc);
                    } else {
                        // Not 7 Bit use the unicode system
                        sb.append("&#");
                        sb.append(new Integer(ch).toString());
                        sb.append(';');
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String encodeSingleChar(String ch) {
        return (String) entityTableEncode.get(ch);
    }
}
