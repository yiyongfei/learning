/**
 * Copyright 2014 伊永飞
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ea.core.storm.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import backtype.storm.LocalDRPC;

import com.ea.core.storm.main.AbstractSubmitTopology;
import com.ea.core.storm.topology.AbstractDRPCTopology;
import com.ea.core.storm.topology.ITopology;

public abstract class AbstractLocalSubmitTopology extends AbstractSubmitTopology {
	private LocalDRPC client = null;
	public AbstractLocalSubmitTopology(){
		super();
		client = new LocalDRPC();
	}
	public LocalDRPC getClient(){
		return this.client;
	}
	
	@Override
	protected Collection<ITopology> _findTopologys() {
		// TODO Auto-generated method stub
		Collection<ITopology> topolotys = this.findTopologys();
		
		List<ITopology> list = new ArrayList<ITopology>();
		ITopology tmp = null;
		String topologyName = null;
		for(ITopology topology : topolotys){
			try {
				topologyName = topology.getTopologyName();
				tmp = topology.getClass().newInstance();
				tmp.setTopologyName(topologyName);
				if(tmp instanceof AbstractDRPCTopology){
					((AbstractDRPCTopology)tmp).setLocalDRPC(this.getClient());
				}
				list.add(tmp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
}
