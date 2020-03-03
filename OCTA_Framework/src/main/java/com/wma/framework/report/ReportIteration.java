package com.wma.framework.report;

public class ReportIteration {
	private int iterationNo;
	private String timeTaken;
	private int numberOfTestsExecuted;
	private int numberOfTestsPassed;
	private int numberOfTestsFailed;
	private String systemName;

	public int getIterationNo() {
		return iterationNo;
	}

	public void setIterationNo(int iterationNo) {
		this.iterationNo = iterationNo;
	}

	public String getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(String timeTaken) {
		this.timeTaken = timeTaken;
	}

	public int getNumberOfTestExecuted() {
		return numberOfTestsExecuted;
	}

	public void setNumberOfTestsExecuted(int numberOfTestsExecuted) {
		this.numberOfTestsExecuted = numberOfTestsExecuted;
	}

	public int getNumberOfTestsPassed() {
		return numberOfTestsPassed;
	}

	public void setNumberOfTestsPassed(int numberOfTestsPassed) {
		this.numberOfTestsPassed = numberOfTestsPassed;
	}

	public int getNumberOfTestsFailed() {
		return numberOfTestsFailed;
	}

	public void setNumberOfTestsFailed(int numberOfTestsFailed) {
		this.numberOfTestsFailed = numberOfTestsFailed;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String[] toStringArray() {
		String[] array = { Integer.toString(iterationNo), timeTaken, Integer.toString(numberOfTestsExecuted),
				Integer.toString(numberOfTestsPassed), Integer.toString(numberOfTestsFailed), systemName };
		return array;
	}

	public String toString() {
		return iterationNo + "," + numberOfTestsExecuted + "," + numberOfTestsPassed + "," + numberOfTestsFailed + ","
				+ systemName;
	}
}
