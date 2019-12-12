package uk.ac.ljmu.fet.cs.cmpgkecs;

import org.apache.commons.lang3.tuple.Pair;

public class BaseCalculator {
	interface ActualOperation {
		int doOperation(Pair<Integer,Integer> t);
	}

	public enum OPERATION {
		ADDITION(t -> t.getLeft()+t.getRight()), MULTIPLY (t -> t.getLeft()*t.getRight());
		private final ActualOperation theRealOperation;

		OPERATION(ActualOperation whatToDo) {
			theRealOperation = whatToDo;
		}

		public int getResult(int a, int b) {
			return theRealOperation.doOperation(Pair.of(a, b));
		}
	};

	int storedA, storedB;
	OPERATION op = OPERATION.ADDITION;

	public BaseCalculator() {
	}

	public BaseCalculator(int a, int b) {
		storedA = a;
		storedB = b;
	}

	public int getFirst() {
		return storedA;
	}

	public int getSecond() {
		return storedB;
	}

	public void setFirst(int a) {
		storedA = a;
	}

	public void setSecond(int b) {
		storedB = b;
	}

	public int solution() {
		return op.getResult(storedA, storedB);
	}

	public OPERATION getOperation() {
		return op;
	}

	public void setOperation(OPERATION op) {
		this.op = op;
	}
}
