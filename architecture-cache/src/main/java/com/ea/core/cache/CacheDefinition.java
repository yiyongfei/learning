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
package com.ea.core.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jodd.props.Props;
import jodd.props.PropsEntry;

import org.springframework.core.io.ClassPathResource;

import com.ea.core.base.CoreDefinition;

public class CacheDefinition {
	static Map<String, String> mapProperty = new HashMap<String, String>();
	
	static{
//静态资源
		ClassPathResource resource = new ClassPathResource(CoreDefinition.getPropertyValue("cache.configure.file"));
		try {
			 Props p = new Props();
			 p.load(resource.getInputStream());
			 Iterator<PropsEntry> entries = p.iterator();
			 while(entries.hasNext()){
				 PropsEntry entry = entries.next();
				 mapProperty.put(entry.getKey(), entry.getValue());
			 }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getPropertyValue(String key){
		return mapProperty.get(key);
	}
}
