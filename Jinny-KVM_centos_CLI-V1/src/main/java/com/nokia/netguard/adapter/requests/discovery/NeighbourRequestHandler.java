
//NAKINA GENERATED HEADER - START
//DO NOT EDIT THE HEADER
//Copyright (c) 2016-2020. Nokia Solutions and Networks Oy. All rights  reserved.
//The modules included herein are subject to a restricted use license and can only  be used in conjunction with this application.
//VERSION: NokiaADK-DEV-18.2.0.15
//DATE: Tue Jun 26 14:14:14 CEST 2018
//CHECKSUM: 2595460780
//NAKINA GENERATED HEADER - FINISH
 
package com.nokia.netguard.adapter.requests.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nakina.adapter.api.responsedatabuilder.topology.NeighbourResponseDataEntryBuilder;
import com.nakina.adapter.base.agent.api.adapterConfiguration.AdapterConfiguration;
import com.nakina.adapter.base.agent.api.associationmgmt.NeighbourRequestHandlerBase;
import com.nakina.adapter.base.agent.api.base.RequestFailure;

 /*
  * This class identifies network elements that are close to this NE (typically one-hop away).
  *
  * @author robertw
  */
public class NeighbourRequestHandler extends NeighbourRequestHandlerBase {

    protected String  neLineTermination = null;
    protected Integer cliCommandTimeoutInMilliseconds = null;    

    @Override
    public List<NeighbourResponseDataEntryBuilder> getNeighbourResponseDataEntryList() throws RequestFailure {

        traceInfo(getClass(), "Neighbour Request");
        
        List<NeighbourResponseDataEntryBuilder> builderList = new ArrayList<>();

        AdapterConfiguration config = new AdapterConfiguration(this);
        neLineTermination = config.getNeLineTermination();       
        cliCommandTimeoutInMilliseconds = config.getIntegerValue("interfaceType.cli.defaultTimeoutInMilliseconds", 60000);

        // Identify any neighbouring network elements (typically one-hop away), and create a 
        // NeighbourResponseDataEntryBuilder for each.  The data stored in the builder must be sufficient
        // to create the NE in the application.  NE Name and IP address (and port number) are most important
        // as well as the name of the interface used to contact the NE.  A session address is also required.
        
        return builderList;
    }

	@Override
	public List<String> getNeighbourIPList() throws RequestFailure {
	    // This method is deprecated.  Do not implement.
		return Collections.emptyList();
	}

	@Override
	public List<String> getNeighbourNeList() throws RequestFailure {
	    // This method is deprecated.  Do not implement.
		return Collections.emptyList();
	}
}