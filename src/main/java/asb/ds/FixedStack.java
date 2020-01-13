package asb.ds;

/**
 * A stack data structure containing a fixed amount of elements.
 * If the stack is full, the oldest elements are overwritten by newly added elements.
 * @author perry
 *
 * @param <T>
 */
public class FixedStack<T> {
	
	private T[] stack;
	private int top;
	private int size;
	
	public FixedStack(int size) {
		this.stack = (T[]) new Object[size];
		this.top = -1; // indicates the stack is empty
		this.size = size;
	}
	
	/**
	 * Push an item onto the top of the stack.
	 * @param item
	 * @return True if successful, false otherwise
	 */
	public boolean push(T item) {
		top = (top + 1) % this.size;
		this.stack[this.top] = item;
		return false;
	}
	
	/**
	 * Get the 1st element (top) in the stack
	 * @return 1st element
	 */
	public T top() {
		return this.stack[this.top];
	}
	
	/**
	 * Get the nth added element in the stack.
	 * @param n The index of the element in the stack (0 = 1st, 1 = 2nd, 2 = 3rd...)
	 * @return The nth element, or null if n is more than the stack's size
	 */
	public T nthTop(int n) {
		if (n >= 0 && n < this.size) {
			//System.out.printf("top=%d, size=%d, n=%d, intended=%d\n", this.top, this.size, n, this.top - n);
			
			int nIndex = this.top - n;
			return this.stack[(nIndex % this.size + this.size) % this.size];
		}
		return null;
	}
	
	
	/**
	 * Fill the entire FixedStack with the given element.
	 * @param element The element to fill this FixedStack
	 */
	public void fill(T element) {
		for (int i = 0; i < this.size; i++) {
			this.push(element);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("STACK [\n");
		for (int i = 0; i < size; i++) {
			String disp = (stack[i] != null) ? stack[i].toString() : "null";
			sb.append("\t" + disp + ((i == size-1) ? "" : ",\n"));
		}
		sb.append("\n]\n");
		
		return sb.toString();
	}
	
	/* FOR DEBUGGING */
	public void prArr() {
		for (int i = 0; i < size; i++) {
			String disp = (stack[i] != null) ? stack[i].toString() : "null";
			System.out.println(disp);
		}
	}
}
