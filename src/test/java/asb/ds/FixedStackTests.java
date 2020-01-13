package asb.ds;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import asb.ds.FixedStack;

public class FixedStackTests {

	FixedStack<Integer> fixedStack;
	
	@Before
	public void initBeforeTests() {
		fixedStack = new FixedStack<Integer>(3);
	}
	
	@Test
	public void test_fill() {
		fixedStack.fill(8);
		assertEquals(8, fixedStack.top().intValue());
		assertEquals(8, fixedStack.nthTop(1).intValue());
		assertEquals(8, fixedStack.nthTop(2).intValue());
	}
	
	@Test
	public void test() {
		fixedStack.push(2);
		assertEquals(2, fixedStack.top().intValue());
		assertNull(fixedStack.nthTop(1));
		assertNull(fixedStack.nthTop(2));
		assertNull(fixedStack.nthTop(3));

		fixedStack.push(7);
		assertEquals(7, fixedStack.top().intValue());
		assertEquals(2, fixedStack.nthTop(1).intValue());
		assertNull(fixedStack.nthTop(2));

		fixedStack.push(3);
		assertEquals(3, fixedStack.top().intValue());
		assertEquals(7, fixedStack.nthTop(1).intValue());
		assertEquals(2, fixedStack.nthTop(2).intValue());

		fixedStack.push(38);
		assertEquals(38, fixedStack.top().intValue());
		assertEquals(3, fixedStack.nthTop(1).intValue());
		assertEquals(7, fixedStack.nthTop(2).intValue());
	}

}
