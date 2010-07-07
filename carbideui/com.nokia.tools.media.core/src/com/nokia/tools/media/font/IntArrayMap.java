/*
* Copyright (c) 2006-2010 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
*/




package com.nokia.tools.media.font;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Implementation of {@link IntMap} interface based on sorted array list.
 *
 */
public class IntArrayMap implements IntMap {

    private static Iterator emptyIterator = null;

    /** The actual storage for the map elements */
    private List list = null;

    /* views */
    private Set entrySet = null;
    private Collection values = null;

    /**
     * Creates an empty <code>IntArrayMap</code>
     */
    public IntArrayMap() {
    }

    /**
     * Creates an instance of <code>IntArrayMap</code> and fills it with
     * the contents of another map
     */
    public IntArrayMap(IntMap map) {
        putAll(map);
    }

    //=======================================================================
    //      I N T E R F A C E
    //=======================================================================

    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return (list == null) ? 0 : list.size();
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return (size() == 0);
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(int key) {
        if (size() > 0) {
            return (lookup(key) >= 0);
        }
        return false;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.  More formally, returns <tt>true</tt> if and only if
     * this map contains at least one mapping to a value <tt>v</tt> such that
     * <tt>(value==null ? v==null : value.equals(v))</tt>.  This operation
     * will probably require time linear in the map size for most
     * implementations of the <tt>Map</tt> interface.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     */
    public boolean containsValue(Object value) {
        int n = size();
        for (int i=0; i<n; i++) {
            Entry entry = (Entry)list.get(i);
            if (equals(entry.getValue(), value)) return true;
        }
        return false;
    }

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.  A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *	       <tt>null</tt> if the map contains no mapping for this key.
     *
     * @see #containsKey(int)
     */
    public Object get(int key) {
        if (list != null) {
            int i = lookup(key);
            if (i >= 0) {
                return ((Entry)list.get(i)).getValue();
            }
        }
        return null;
    }

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation).  If the map previously contained a mapping for
     * this key, the old value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the map previously associated <tt>null</tt>
     *	       with the specified key, if the implementation supports
     *	       <tt>null</tt> values.
     *
     * @throws UnsupportedOperationException if the <tt>put</tt> operation is
     *	          not supported by this map.
     * @throws ClassCastException if the class of the specified value
     * 	          prevents it from being stored in this map.
     * @throws IllegalArgumentException if some aspect of this key or value
     *	          prevents it from being stored in this map.
     * @throws NullPointerException this map does not permit <tt>null</tt>
     *            values, and the value is <tt>null</tt>.
     */
    public Object put(int key, Object value) {
        Object oldValue = null;
        if (list == null) list = new ArrayList();
        int i = lookup(key);
        if (i >= 0) {
            Entry entry = (Entry)list.get(i);
            oldValue = entry.getValue();
            entry.setValue(value);
        } else {
           list.add(-(i+1), new MapEntry(key, value));
        }
        return oldValue;
    }

    /**
     * Removes the mapping for this key from this map if present (optional
     * operation).
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the map previously associated <tt>null</tt>
     *	       with the specified key, if the implementation supports
     *	       <tt>null</tt> values.
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *         not supported by this map.
     */
    public Object remove(int key) {
        Object oldValue = null;
        if (list != null) {
            int i = lookup(key);
            if (i >= 0) {
                Entry entry = (Entry)list.remove(i);
                oldValue = entry.getValue();
                entry.setValue(null);   // clear reference
            }
        }
        return oldValue;
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * (optional operation).  These mappings will replace any mappings that
     * this map had for any of the keys currently in the specified map.
     *
     * @param map Mappings to be stored in this map.
     *
     * @throws UnsupportedOperationException if the <tt>putAll</tt> method is
     * 		  not supported by this map.
     *
     * @throws ClassCastException if the class of a key or value in the
     * 	          specified map prevents it from being stored in this map.
     *
     * @throws IllegalArgumentException some aspect of a key or value in the
     *	          specified map prevents it from being stored in this map.
     *
     * @throws NullPointerException this map does not permit <tt>null</tt>
     *            keys or values, and the specified key or value is
     *            <tt>null</tt>.
     */
    public void putAll(IntMap map) {
        if (map != this && !map.isEmpty()) {
            Iterator i = map.entrySet().iterator();
            while (i.hasNext()) {
                Entry entry = (Entry)i.next();
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Removes all mappings from this map (optional operation).
     *
     * @throws UnsupportedOperationException clear is not supported by this
     * 		  map.
     */
    public void clear() {
        if (list != null) list.clear();
    }

    /**
     * Returns a set of keys contained in this map.
     * @return a set of keys contained in this map.
     */
    public int [] keys() {
        int n = size();
        int [] keys = new int[n];
        for (int i=0; i<n; i++) {
            Entry entry = (Entry)list.get(i);
            keys[i] = entry.getKey();
        }
        return keys;
    }

    /**
     * Returns a collection view of the values contained in this map.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  If the map is modified while an
     * iteration over the collection is in progress, the results of the
     * iteration are undefined.  The collection supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations.
     * It does not support the add or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection values() {
        if (values==null) {
            values = new Values();
        }
        return values;
    }

    /**
     * Returns a set view of the mappings contained in this map.  Each element
     * in the returned set is a <tt>Map.Entry</tt>.  The set is backed by the
     * map, so changes to the map are reflected in the set, and vice-versa.
     * If the map is modified while an iteration over the set is in progress,
     * the results of the iteration are undefined.  The set supports element
     * removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not support
     * the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map.
     */
    public Set entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    //=======================================================================
    //      I M P L E M E N T A T I O N
    //=======================================================================

    /**
     * Looks up map entry given its key.
     * @return index of the found entry, if it is contained in the list;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the list: the index of the first
     *	       element greater than the key, or <tt>list.size()</tt>, if all
     *	       elements in the list are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     */
    private int lookup(int key) {
        int low = 0;
        int high = list.size()-1;
        while (low <= high) {
            int mid =(low + high)/2;
            int midVal = ((Entry)list.get(mid)).getKey();
            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found
    }

    //=======================================================================
    //      I N N E R    C L A S S E S
    //=======================================================================

    /**
     * A map entry (key-value pair).
     */
    private class MapEntry implements Entry, Comparable {

        private int key;
        private Object value;

        MapEntry(Entry entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        MapEntry(int key, Object value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the key corresponding to this entry.
         * @return the key corresponding to this entry.
         */
        public int getKey() {
            return key;
        }

        /**
         * Returns the value corresponding to this entry.  If the mapping
         * has been removed from the backing map (by the iterator's
         * <tt>remove</tt> operation), the results of this call are undefined.
         *
         * @return the value corresponding to this entry.
         */
        public Object getValue() {
            return value;
        }

        /**
         * Replaces the value corresponding to this entry with the specified
         * value (optional operation).  (Writes through to the map.)  The
         * behavior of this call is undefined if the mapping has already been
         * removed from the map (by the iterator's <tt>remove</tt> operation).
         *
         * @param value new value to be stored in this entry.
         * @return old value corresponding to the entry.
         */
        public Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        /**
         * Compares this object with the specified object.
         * @param   o the Object to be compared.
         */
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof Entry) {
                Entry that = (Entry)o;
                return (that.getKey() == key &&
                        IntArrayMap.equals(that.getValue(), value));
            }
            return false;
        }

        /**
         * Returns the hashcode for this object
         */
        public int hashCode() {
            return key;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.<p>
         *
         *
         * @param   o the Object to be compared.
         * @return  a negative integer, zero, or a positive integer as this object
         *		is less than, equal to, or greater than the specified object.
         *
         * @throws ClassCastException if the specified object's type prevents it
         *         from being compared to this Object.
         */
        public int compareTo(Object o) {
            if (o == this) return 0;
            int otherKey = ((Entry)o).getKey();
            if (key < otherKey) {
                return -1;
            } else if (key > otherKey) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * A collection view of the values contained in this map. The
     * collection is backed by the map, so changes to the map are reflected
     * in the collection, and vice-versa.  If the map is modified while an
     * iteration over the collection is in progress, the results of the
     * iteration are undefined.  The collection supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations.
     * It does not support the add or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map.
     */
    private class Values extends AbstractCollection {
        public Iterator iterator() {
            if (IntArrayMap.this.isEmpty()) {
                if (emptyIterator == null) {
                    emptyIterator = new EmptyIterator();
                }
                return emptyIterator;
            }
            return new ValuesIterator();
        }
        public int size() {
            return IntArrayMap.this.size();
        }
        public void clear() {
            IntArrayMap.this.clear();
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
    }

    private class EntrySet extends AbstractSet {
        public int size() {
            return IntArrayMap.this.size();
        }
        public void clear() {
            IntArrayMap.this.clear();
        }
        public Iterator iterator() {
            return new EntriesIterator();
        }
        public boolean contains(Object o) {
            if (o instanceof Entry) {
                Entry entry = (Entry)o;
                int key = entry.getKey();
                int i = lookup(key);
                if (i >= 0) {
                    Entry e = (Entry)list.get(i);
                    return e.equals(entry);
                }
            }
            return false;
        }
        public boolean remove(Object o) {
            if (o instanceof Entry) {
                Entry entry = (Entry)o;
                int key = entry.getKey();
                int i = lookup(key);
                if (i >= 0) {
                    Entry e = (Entry)list.get(i);
                    if (e.equals(entry)) {
                        list.remove(i);
                        e.setValue(null);
                    }
                }
            }
            return false;
        }
    }

    /**
     * Iterator over all entries in the map
     */
    private class EntriesIterator implements Iterator {
        private int i = 0;
        private boolean removed = false;
        public boolean hasNext() {
            return (i < size());
        }
        public Object next() {
            if (!hasNext()) throw new NoSuchElementException();
            removed = false;
            return list.get(i++);
        }
        public void remove() {
            if (i == 0) throw new NoSuchElementException();
            if (removed) throw new IllegalStateException();
            removed = true;
            list.remove(--i);
        }
    }

    /**
     * Iterator over all values in the map
     */
    private class ValuesIterator extends EntriesIterator {
        public Object next() {
            Entry entry = (Entry)super.next();
            return entry.getValue();
        }
    }

    /**
     * Empty iterator
     */
    private static final class EmptyIterator implements Iterator {
        public boolean hasNext() { return false; }
        public Object next() { throw new NoSuchElementException(); }
        public void remove() { throw new IllegalStateException(); }
    }

    /**
     * Tests if two objects "equal to" each other. The objects are considered
     * to be equal if either of the following conditions holds true:
     * <ul>
     * <li>both objects are <code>null</code>.
     * <li>the objects are identical to each other (<code>o1 == o2</code>)
     * <li><code>o1</code> is not <code>null</code> and
     *     <code>o1.equals(o2)</code> returns <code>true</code>.
     * <li><code>o2</code> is not <code>null</code> and
     *     <code>o2.equals(o1)</code> returns <code>true</code>.
     * </ul>
     * Otherwise, this function returns <code>false</code>.
     */
    public static boolean equals(Object o1, Object o2) {
        boolean equal = false;
        if (o1 == o2) {
            equal = true;
        } else if (o1 != null) {
            equal = o1.equals(o2);
        } else if (o2 != null) {
            equal = o2.equals(o1);
        }
        return equal;
    }
}
