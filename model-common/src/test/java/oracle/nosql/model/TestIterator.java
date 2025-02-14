/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import oracle.nosql.model.util.LazyIteratorChain;
import oracle.nosql.model.util.PushbackIterator;
import oracle.nosql.model.util.SingleItemIterator;

public class TestIterator {

	@Test
	public void testSingleIterator() {
		String single = "single";
		Iterator<String> iterator = new SingleItemIterator<>(single);
		
		assertTrue(iterator.hasNext());
		assertEquals(single, iterator.next());
		assertFalse(iterator.hasNext());
	}
	
	@Test
	public void testPushbackIterator() {
		List<String> list = Arrays.asList("one", "two", "three");
		Iterator<String> iterator = new PushbackIterator<>("zero", list.iterator());
		
		assertEquals(list.size()+1, count(iterator));
	}

	
	@Test
	public void iterateAcrossMultipleCollection() {
		final List<Integer> elements1 = Arrays.asList(1,2,3,4);
		final List<Integer> elements2 = Arrays.asList(5,6,7,8);
		
		Iterator<Integer> test = new LazyIteratorChain<Integer>() {
			@Override
			protected Iterator<Integer> nextIterator(int iteratorCount) {
				switch (iteratorCount) {
				case 0: return elements1.iterator();
				case 1: return elements2.iterator();
				default : return null;
				}
			}
		};
		
		assertEquals(elements1.size()+elements2.size(), count(test));
			
	}
	
	@Test
	public void iterateOfEmptyCollectionIsEmpty() {
		final List<String> empty = new ArrayList<>();
		
		Iterator<String> test = new LazyIteratorChain<String>() {
			@Override
			protected Iterator<String> nextIterator(int iteratorCount) {
				return iteratorCount == 0  ? empty.iterator() : null;
			}
		};
		assertFalse(test.hasNext());
	}
	
	@Test
	public void iterateOfNullCollectionIsEmpty() {
		Iterator<String> test = new LazyIteratorChain<String>() {
			@Override
			protected Iterator<String> nextIterator(int iteratorCount) {
				return null;
			}
		};
		assertFalse(test.hasNext());
	}
	
	@Test
	public void iteratorTerminatesOnNullIteratorInChain() {
		final List<Integer> elements1 = Arrays.asList(1,2,3,4);
		final List<Integer> elements3 = Arrays.asList(1,2,3,4);
		
		Iterator<Integer> test = new LazyIteratorChain<Integer>() {
			@Override
			protected Iterator<Integer> nextIterator(int iteratorCount) {
				switch (iteratorCount) {
				case 0: return elements1.iterator();
				case 1: return null;
				case 2: return elements3.iterator();
				default: fail("not expected to be called on next iterator "
						+ iteratorCount);
						return null;
				}
			}
		};
		
		assertEquals(elements1.size(), count(test));
			
	}
	
	int count(Iterator<?> iterator) {
		int count = 0;
		//System.err.println("" + iterator);
		while (iterator.hasNext()) {
			iterator.next();
			//System.err.println("" + count + " :" + e);
			count++;
		}
		return count;
		
	}




}
