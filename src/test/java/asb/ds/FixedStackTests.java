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
	public void test() {
		fixedStack.push(2);
		assertEquals(2, fixedStack.top().intValue());
	}

}
