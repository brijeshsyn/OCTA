package com.wma.framework.api.model;

public class ResourceParameter {
	private String name;
	private String stepNumberReference;
	private String queryOrResponse;
	private String key;
	
	public String getName() {
		return name;
	}
	public ResourceParameter setName(String name) {
		this.name = name;
		return this ;
	}
	public String getStepNumberReference() {
		return stepNumberReference;
	}
	public ResourceParameter setStepNumberReference(String stepNumberReference) {
	    this.stepNumberReference = stepNumberReference;
	    return this;
    } 
    public String getQueryOrResponse() {
    	return queryOrResponse;
    }
    public ResourceParameter setQueryOrResponse(String queryOrResponse) {
         this.queryOrResponse = queryOrResponse;
         return this;
    }
    public String getKey() {
    	return key;
    }
    public ResourceParameter setKey(String key) {
    	this.key = key;
    	return this;
    }
    
    
}    
   