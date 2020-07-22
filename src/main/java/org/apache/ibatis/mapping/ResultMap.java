/*
 *    Copyright 2009-2013 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
/**
 * 结果映射
 * MyBatis 中最重要最强大的元素
 *
 * 1. ResultMap对象是结果集中的一行记录和一个java对象的对应关系。
 * 2. ResultMap由id,type等基本的属性组成外，还包含多个ResultMapping对象。这类似于一个java对象由多个属性组成一个道理。
 *
 *
 * <resultMap type="com.ashan.mybatis.User" id="detailUserResultMap"> <!-- 整个resultMap会被解析成一个ResultMap对应 -->
 *
 * 		<constructor>
 * 			<idArg column="user_id" javaType="String"/>  <!-- idArg会被解析成一个resultMapping对象 -->
 * 			<arg column="user_name"/> <!-- resultMapping对象 -->
 * 		</constructor>
 *
 * 		<result property="password" column="user_pwd" /> <!-- resultMapping对象 -->
 * 		<result property="type" column="user_type" javaType="com.ashan.mybatis.UserType" <!-- resultMapping对象 -->
 * 		       typeHandler="com.ashan.mybatis.UserTypeHandler"/>
 * 		<result property="svcnum" column="svc_num" /> <!-- resultMapping对象 -->
 *
 * 		<association property="cust" javaType="com.ashan.mybatis.Cust"> <!-- resultMapping对象 这个resultMapping对象指向了另一个ResultMap-->
 * 			<id property="id" column="cust_id"/>
 * 			<result property="custname" column="cust_name"/>
 * 			<result property="certNo" column="cert_no"/>
 * 		</association>
 *
 * 		<collection property="accts" ofType="com.ashan.mybatis.Acct">
 * 			<id property="id" column="acct_id" />
 * 			<result property="payName" column="pay_name"/>
 * 			<result property="bankNo" column="bank_no"/>
 * 		</collection>
 *
 * 	</resultMap>
 *
 */
public class ResultMap {
  private String id;
  private Class<?> type;
  //所有的resultMapping对象，包括constructor/idArg,constructor/arg,result,association,collection,但不包括association和collection里的子节点
  private List<ResultMapping> resultMappings;
  //包括constructor/idArg,id
  private List<ResultMapping> idResultMappings;
  //constructor里的子节点
  private List<ResultMapping> constructorResultMappings;
  //除constructor里的子节点,其他都是，result,association,collection,id
  private List<ResultMapping> propertyResultMappings;
  //所有被映射的列
  private Set<String> mappedColumns;
  //比较少用
  private Discriminator discriminator;
  //是否有内映射，上图中association, collection都为内映射,内查询不算（就是的reulst节点中配置select属性的情况）
  private boolean hasNestedResultMaps;
  //是否有查询
  private boolean hasNestedQueries;
  //是否要求自动映射
  private Boolean autoMapping;

  private ResultMap() {
  }

  //静态内部类，建造者模式
  public static class Builder {
    private ResultMap resultMap = new ResultMap();

    public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings) {
      this(configuration, id, type, resultMappings, null);
    }

    public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings, Boolean autoMapping) {
      resultMap.id = id;
      resultMap.type = type;
      resultMap.resultMappings = resultMappings;
      resultMap.autoMapping = autoMapping;
    }

    public Builder discriminator(Discriminator discriminator) {
      resultMap.discriminator = discriminator;
      return this;
    }

    public Class<?> type() {
      return resultMap.type;
    }

    public ResultMap build() {
      if (resultMap.id == null) {
        throw new IllegalArgumentException("ResultMaps must have an id");
      }
      resultMap.mappedColumns = new HashSet<String>();
      resultMap.idResultMappings = new ArrayList<ResultMapping>();
      resultMap.constructorResultMappings = new ArrayList<ResultMapping>();
      resultMap.propertyResultMappings = new ArrayList<ResultMapping>();
      for (ResultMapping resultMapping : resultMap.resultMappings) {
        resultMap.hasNestedQueries = resultMap.hasNestedQueries || resultMapping.getNestedQueryId() != null;
        resultMap.hasNestedResultMaps = resultMap.hasNestedResultMaps || (resultMapping.getNestedResultMapId() != null && resultMapping.getResultSet() == null);
        final String column = resultMapping.getColumn();
        if (column != null) {
          resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
        } else if (resultMapping.isCompositeResult()) {
          for (ResultMapping compositeResultMapping : resultMapping.getComposites()) {
            final String compositeColumn = compositeResultMapping.getColumn();
            if (compositeColumn != null) {
              resultMap.mappedColumns.add(compositeColumn.toUpperCase(Locale.ENGLISH));
            }
          }
        }
        if (resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR)) {
          resultMap.constructorResultMappings.add(resultMapping);
        } else {
          resultMap.propertyResultMappings.add(resultMapping);
        }
        if (resultMapping.getFlags().contains(ResultFlag.ID)) {
          resultMap.idResultMappings.add(resultMapping);
        }
      }
      if (resultMap.idResultMappings.isEmpty()) {
        resultMap.idResultMappings.addAll(resultMap.resultMappings);
      }
      // lock down collections
      resultMap.resultMappings = Collections.unmodifiableList(resultMap.resultMappings);
      resultMap.idResultMappings = Collections.unmodifiableList(resultMap.idResultMappings);
      resultMap.constructorResultMappings = Collections.unmodifiableList(resultMap.constructorResultMappings);
      resultMap.propertyResultMappings = Collections.unmodifiableList(resultMap.propertyResultMappings);
      resultMap.mappedColumns = Collections.unmodifiableSet(resultMap.mappedColumns);
      return resultMap;
    }
  }

  public String getId() {
    return id;
  }

  public boolean hasNestedResultMaps() {
    return hasNestedResultMaps;
  }

  public boolean hasNestedQueries() {
    return hasNestedQueries;
  }

  public Class<?> getType() {
    return type;
  }

  public List<ResultMapping> getResultMappings() {
    return resultMappings;
  }

  public List<ResultMapping> getConstructorResultMappings() {
    return constructorResultMappings;
  }

  public List<ResultMapping> getPropertyResultMappings() {
    return propertyResultMappings;
  }

  public List<ResultMapping> getIdResultMappings() {
    return idResultMappings;
  }

  public Set<String> getMappedColumns() {
    return mappedColumns;
  }

  public Discriminator getDiscriminator() {
    return discriminator;
  }

  public void forceNestedResultMaps() {
    hasNestedResultMaps = true;
  }
  
  public Boolean getAutoMapping() {
    return autoMapping;
  }

}
