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
package com.ea.core.orm.handle.impl;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.ea.core.base.po.BasePO;
import com.ea.core.cache.CacheConstants;
import com.ea.core.cache.annotation.Cache;
import com.ea.core.orm.handle.AbstractORMHandle;
import com.ea.core.orm.handle.ORMConstants;
import com.ea.core.orm.handle.ORMHandle;
import com.ea.core.orm.handle.dto.ORMParamsDTO;

/**
 * 更新操作，由Hibnerate完成
 * 
 * @author yiyongfei
 *
 */
@Component
public class UpdateORMHandle extends AbstractORMHandle {
    
	public UpdateORMHandle() {
		super(ORMConstants.ORM_LEVEL.UPDATE.getCode());
	}

	@Cache(CacheConstants.CommandType.SET)
	@Override
	protected Object execute(ORMParamsDTO dto) throws Exception {
		Object po = dto.getParam();
		if(po instanceof BasePO){
			Session session = this.getHibernateSessionFactory().getCurrentSession();
			session.update(po);
	        return null;
		} else {
			throw new Exception("参数请确认是否继承BasePO!");
		}

	}

	@Override
	public void setNextHandle() {
		// TODO Auto-generated method stub
		ORMHandle nextHandle = (LoadORMHandle)this.getApplicationContext().getBean("loadORMHandle");
		this.setNextHandle(nextHandle);
	}
	
}
