package com.ettrema.web;

import com.ettrema.web.calc.Calc;
import com.ettrema.web.children.ChildrenOfTypeMap;
import com.ettrema.web.component.ComponentValue;
import java.util.*;
import org.apache.log4j.Logger;

public class BaseResourceList extends ArrayList<Templatable> {

    private static final Logger log = Logger.getLogger(BaseResourceList.class);
    private static final long serialVersionUID = 1L;
    private final Map<String, Templatable> map = new HashMap<>();

    public BaseResourceList() {
    }

    public BaseResourceList(Templatable[] array) {
        addAll(Arrays.asList(array));
    }    
    
    public BaseResourceList(BaseResourceList copyFrom) {
        super(copyFrom);
    }

    @Override
    public boolean add(Templatable e) {
        if (e == null) {
            throw new NullPointerException("Attempt to add null node");
        }
        if (e.getName() == null) {
            throw new NullPointerException("Attempt to add resource with null name: " + e.getClass().getName());
        }
        if (map.containsKey(e.getName())) {
//            Exception ex = new Exception("identical child names");
            log.debug("identical child names: " + e.getName());//,ex);
            Templatable cur = map.get(e.getName());
            if (cur.getModifiedDate() == null) {
                remove(cur);
            } else if (e.getModifiedDate() == null) {
                // ignore
                return true;
            } else {
                if (e.getModifiedDate().after(cur.getModifiedDate())) {
                    remove(cur);
                } else {
                    return true;
                }
            }
        }
        map.put(e.getName(), e);
        boolean b = super.add(e);
        return b;
    }
    
    /**
     * Just adds the elements in the given list to this list and returns
     * list to make it suitable for chaining and use from velocity
     * 
     * @param otherList
     * @return 
     */
    public BaseResourceList add(BaseResourceList otherList) {
        addAll(otherList);
        return this;
    }

    public Templatable get(String name) {
        return map.get(name);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Templatable) {
            Templatable e = (Templatable) o;
            map.remove(e.getName());
        }
        int i = super.indexOf(o);
        Object removed = super.remove(i);
        return (removed != null);
    }

    public Templatable getFirst() {
        if( isEmpty() ) {
            return null;
        }
        return this.get(0);
    }

    public Templatable getLast() {
        if (this.size() > 0) {
            return this.get(this.size() - 1);
        } else {
            return null;
        }
    }

    /**
     * Return the first resource of the given type
     * 
     * @param type
     * @return 
     */
    public Templatable first(String type) {
        for (Templatable res : this) {
            if (res.is(type)) {
                return res;
            }
        }
        return null;
    }

    public Templatable getRandom() {
        return random(null);
    }

    /**
     *
     * @param type
     * @return - a random item in this list, but which satisfies the "is" test
     * on type
     */
    public Templatable random(String type) {
        int l = this.size();
        if (l == 0) {
            return null;
        }

        List<Templatable> list = new ArrayList<>();
        for (Templatable res : this) {
            if (type == null || res.is(type)) {
                list.add(res);
            }
        }
        if (list.isEmpty()) {
            return null;
        }

        Random rnd = new Random();
        int pos = rnd.nextInt(list.size());
        return list.get(pos);
    }

    /**
     *
     * @return - a copy of this list, with any Link items replaced with their
     * destination resource
     */
    public BaseResourceList getResolveLinks() {
        BaseResourceList list = new BaseResourceList(this);
        for (Templatable r : this) {
            if (r instanceof Link) {
                Link link = (Link) r;
                list.add(link.getDest());
            } else {
                list.add(r);
            }
        }
        return list;
    }

    public BaseResourceList getReverse() {
        BaseResourceList list = new BaseResourceList(this);
        Collections.reverse(list);
        return list;
    }

    public BaseResourceList getSortByCreatedDate() {
        BaseResourceList list = new BaseResourceList(this);
        Collections.sort(list, new Comparator<Templatable>() {

            @Override
            public int compare(Templatable o1, Templatable o2) {
                Date dt1 = o1.getCreateDate();
                Date dt2 = o2.getCreateDate();
                if (dt1 == null) {
                    return -1;
                }
                return -1 * dt1.compareTo(dt2);
            }
        });
        return list;
    }

    public BaseResourceList getSortByModifiedDate() {
        BaseResourceList list = new BaseResourceList(this);
        Collections.sort(list, new Comparator<Templatable>() {

            @Override
            public int compare(Templatable o1, Templatable o2) {
                Date dt1 = o1.getModifiedDate();
                Date dt2 = o2.getModifiedDate();
                if (dt1 == null) {
                    return -1;
                }
                return -1 * dt1.compareTo(dt2);
            }
        });
        return list;
    }

    public Calc getCalc() {
        return new Calc(this);
    }

    public BaseResourceList where(String mvelExpr) {
        return getCalc().filter(mvelExpr);
    }

    public BaseResourceList sortByField(final String fieldName) {
        BaseResourceList list = new BaseResourceList(this);
        Collections.sort(list, new Comparator<Templatable>() {

            @Override
            public int compare(Templatable o1, Templatable o2) {
                ComponentValue cv1 = o1.getValues().get(fieldName);
                ComponentValue cv2 = o2.getValues().get(fieldName);
                Object val1 = cv1 == null ? null : cv1.typedValue(o1);
                Object val2 = cv2 == null ? null : cv2.typedValue(o2);

                if (val1 == null) {
                    if (val2 == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else {
                    if (val1 instanceof Comparable) {
                        try {
                            Comparable c1 = (Comparable) val1;
                            if (val2 != null) {
                                return c1.compareTo(val2);
                            } else {
                                return 1;
                            }
                        } catch (Throwable e) {
                            log.warn("failed to compare: " + val1 + " - " + val2);
                            return -1;
                        }
                    } else {
                        return -1;
                    }
                }

            }
        });
        return list;
    }

    /**
     * Sort by a MVEL expression
     *
     * @param expr - the expression, evaluated in the context of each member of
     * the list
     * @return
     */
    public BaseResourceList sortBy(final String expr) {
        BaseResourceList list = new BaseResourceList(this);
        Collections.sort(list, new Comparator<Templatable>() {

            @Override
            public int compare(Templatable o1, Templatable o2) {
                if (o1 instanceof Page && o2 instanceof Page) {
                    Page p1 = (Page) o1;
                    Page p2 = (Page) o2;
                    Object val1 = getCalc().eval(expr, p1);
                    Object val2 = getCalc().eval(expr, p2);
                    if (val1 == null) {
                        if (val2 == null) {
                            return 0;
                        } else {
                            return -1;
                        }
                    } else {
                        if (val1 instanceof Comparable) {
                            try {
                                Comparable c1 = (Comparable) val1;
                                if (val2 != null) {
                                    return c1.compareTo(val2);
                                } else {
                                    return 1;
                                }
                            } catch (Throwable e) {
                                log.warn("failed to compare: " + val1 + " - " + val2);
                                return -1;
                            }
                        } else {
                            return -1;
                        }
                    }
                } else {
                    return 0;
                }

            }
        });
        return list;
    }

    public BaseResourceList getRandomSort() {
        Templatable[] array = new Templatable[this.size()];
        this.toArray(array);
        
        Random rng = new Random();   // i.e., java.util.Random.
        int n = array.length;        // The number of items left to shuffle (loop invariant).
        while (n > 1) {
            int k = rng.nextInt(n);  // 0 <= k < n.
            n--;                     // n is now the last pertinent index;
            Templatable temp = array[n];     // swap array[n] with array[k] (does nothing if k == n).
            array[n] = array[k];
            array[k] = temp;
        }
        BaseResourceList newList = new BaseResourceList(array);
        return newList;
    }

    public BaseResourceList exclude(String s) {
        return _exclude(s);
    }

    public BaseResourceList exclude(String s1, String s2) {
        return _exclude(s1, s2);
    }

    public BaseResourceList exclude(String s1, String s2, String s3) {
        return _exclude(s1, s2, s3);
    }

    public BaseResourceList exclude(String s1, String s2, String s3, String s4) {
        return _exclude(s1, s2, s3, s4);
    }

    public BaseResourceList _exclude(String... s) {
        BaseResourceList newList = new BaseResourceList(this);
        Iterator<Templatable> it = newList.iterator();
        while (it.hasNext()) {
            Templatable ct = it.next();
            if (contains(s, ct.getName())) {
                it.remove();
            }
        }
        return newList;
    }

    private boolean contains(String[] arr, String name) {
        for (String s : arr) {
            if (name.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a new list where elements satisfy is(s)
     *
     * @param s
     * @return
     */
    public BaseResourceList ofType(String s) {
        BaseResourceList newList = new BaseResourceList(this);
        Iterator<Templatable> it = newList.iterator();
        while (it.hasNext()) {
            Templatable ct = it.next();
            if (!ct.is(s)) {
                it.remove();
            }
        }
        return newList;
    }

    public Map<Object, BaseResourceList> groupByField(final String fieldName) {
        Map<Object, BaseResourceList> groups = new HashMap<Object, BaseResourceList>();
        for (Templatable t : this) {
            ComponentValue keyCv = t.getValues().get(fieldName);
            Object key = keyCv.getValue();
            BaseResourceList val = groups.get(key);
            if (val == null) {
                val = new BaseResourceList();
                groups.put(key, val);
            }
            val.add(t);
        }
        return groups;
    }

    /**
     * Return a new list with a size no greater then the given argument
     *
     * @param maxSize - the maximum number of elements in the new list
     * @return
     */
    public BaseResourceList truncate(int maxSize) {
        BaseResourceList list = new BaseResourceList();
        for (int i = 0; i < maxSize && i < size(); i++) {
            list.add(get(i));
        }
        return list;
    }
    
    public Map<String,BaseResourceList> getOfType() {
        return new ChildrenOfTypeMap(this);
    }
    
    public List<UUID> toIds() {
        List<UUID> list = new ArrayList<>();
        for(Templatable res : this) {
            if( res instanceof BaseResource) {
                BaseResource bres = (BaseResource) res;
                list.add(bres.getNameNodeId());
            }
        }
        return list;
    }
    
    /**
     * Returns the next item after the one given. If the given argument
     * is null, returns the first item in the list
     * 
     * @param from
     * @return 
     */
    public Templatable next(Templatable from) {
        if( from == null ) {
            return getFirst();
        } else {
            boolean found = false;
            for( Templatable r : this) {
                if( found ) {
                    return r;
                }
                if( r == from) {
                    found = true;
                }
            }
            return null;
        }
    }
}
