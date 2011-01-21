package com.bradmcevoy.web.search;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResourceList;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Templatable;

/**
 *
 * @author brad
 */
public class FolderSearcher {

    private final static FolderSearcher folderSearcher = new FolderSearcher();

    public static FolderSearcher getFolderSearcher() {
        return folderSearcher;
    }

    public BaseResourceList search(Templatable from, Path path) {
        BaseResourceList list = new BaseResourceList();
        _search(from, list, path.getParts(), 0);
        return list;
    }

    private void _search(Templatable from, BaseResourceList list, String[] parts, int index) {
        for (int i = 0; i < parts.length; i++) {
            String s = parts[i];
            if (s.equals(".")) {
                if (index == parts.length - 1) {
                    list.add(from);
                } else {
                    // from = from; // do nothing
                }
            } else if (s.equals("..")) {
                from = from.getParent(); // up one level
            } else if (s.equals("**")) {
                if (from instanceof CollectionResource) {
                    recurse((CollectionResource) from, list, parts, index);
                } else {
                    // can't recurse non-collection
                }
            } else {
                if (from instanceof CollectionResource) {
                    process((CollectionResource) from, list, parts, index);
                }
            }
        }
    }

    /**
     * For each sub folder (recursive) call _search with the next index
     * @param from
     * @param list
     * @param parts
     * @param index
     */
    private void recurse(CollectionResource colFrom, BaseResourceList list, String[] parts, int index) {
        if (index < parts.length - 1) {
            for (Resource r : colFrom.getChildren()) {
                if (r instanceof CommonTemplated) {
                    CommonTemplated nextFrom = (CommonTemplated) r;
                    _search(nextFrom, list, parts, index + 1);
                } else {
                    // ignore
                }
                if (r instanceof CollectionResource) {
                    CollectionResource nextCol = (CollectionResource) r;
                    recurse(nextCol, list, parts, index); // don't increment index, because we're still processing recurse instruction
                }
            }
        } else {
            // ** won't match anything, its just a way of delegating. So if no child spec then won't match anything
        }
    }

    /**
     * 
     * @param from
     * @param list
     * @param string
     * @param isTerminal
     */
    private void process(CollectionResource from, BaseResourceList list, String[] parts, int index) {
        String childSpec = parts[index];
        boolean isTerminal = (index == parts.length - 1);
        if (childSpec.contains("*") || childSpec.contains("[")) {
            // need to evaluate each child
            ChildSpec spec = new ChildSpec(childSpec);
            for (Resource r : from.getChildren()) {
                if (r instanceof Templatable) {
                    Templatable child = (Templatable) r;
                    if (spec.isMatch(child)) {
                        if (isTerminal) {
                            list.add(child);
                        } else {
                            _search(child, list, parts, index + 1);
                        }
                    } else {
                        // ignore
                    }
                } else {
                    // ignore
                }
            }
        } else {
            // simple, just look for child
            Resource child = from.child(childSpec);
            if (child != null) {
                if (child instanceof Templatable) {
                    if (isTerminal) {
                        list.add((Templatable) child);
                    } else {
                        _search((Templatable) child, list, parts, index + 1);
                    }
                } else {
                    // ignore
                }
            } else {
                // not found
            }
        }
    }

    private enum WildcardPos {

        FRONT,
        BACK,
        ALL,
        NONE
    }

    private class ChildSpec {

        private final String namePattern;  // null means match all
        private final String templatePattern;

        public ChildSpec(String spec) {
            if (spec.contains("[")) {
                int p = spec.indexOf("[");
                if (p > 0) {
                    String sName = spec.substring(0, p - 1);
                    if (sName.equals("*")) {
                        namePattern = null; // means match all
                    } else {
                        namePattern = sName;
                    }
                } else {
                    namePattern = null;
                }
                String sTemplate = spec.substring(p + 1);
                if (sTemplate.equals("*")) {
                    templatePattern = null;
                } else {
                    templatePattern = sTemplate;
                }
            } else {
                this.namePattern = spec;
                this.templatePattern = null;
            }
        }

        private boolean isMatch(Templatable child) {
            if (!isMatchName(child.getName())) {
                return false;
            }
            return isMatchTemplate(child.getName());
        }

        private boolean isMatchName(String name) {
            if (namePattern == null) {
                return true;
            } else {
                return namePattern.equals(name);
            }
        }

        private boolean isMatchTemplate(String name) {
            if (templatePattern == null) {
                return true;
            } else {
                return templatePattern.equals(name);
            }

        }
    }
}
