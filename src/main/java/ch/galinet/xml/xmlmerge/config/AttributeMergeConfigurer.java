/*
 * EL4J, the Extension Library for the J2EE, adds incremental enhancements to
 * the spring framework, http://el4j.sf.net
 * Copyright (C) 2006 by ELCA Informatique SA, Av. de la Harpe 22-24,
 * 1000 Lausanne, Switzerland, http://www.elca.ch
 *
 * EL4J is published under the GNU Lesser General Public License (LGPL)
 * Version 2.1. See http://www.gnu.org/licenses/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * For alternative licensing, please contact info@elca.ch
 */
package ch.galinet.xml.xmlmerge.config;

import ch.galinet.xml.xmlmerge.ConfigurationException;
import ch.galinet.xml.xmlmerge.Configurer;
import ch.galinet.xml.xmlmerge.Mapper;
import ch.galinet.xml.xmlmerge.Matcher;
import ch.galinet.xml.xmlmerge.MergeAction;
import ch.galinet.xml.xmlmerge.XmlMerge;
import ch.galinet.xml.xmlmerge.action.OrderedMergeAction;
import ch.galinet.xml.xmlmerge.action.StandardActions;
import ch.galinet.xml.xmlmerge.factory.AttributeOperationFactory;
import ch.galinet.xml.xmlmerge.factory.OperationResolver;
import ch.galinet.xml.xmlmerge.factory.StaticOperationFactory;
import ch.galinet.xml.xmlmerge.mapper.NamespaceFilterMapper;
import ch.galinet.xml.xmlmerge.matcher.StandardMatchers;
import ch.galinet.xml.xmlmerge.matcher.TagMatcher;

/**
 * Configure to apply actions declared as attributes in the patch DOM.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class AttributeMergeConfigurer implements Configurer {

	/**
	 * Attribute namespace.
	 */
	public static final String ATTRIBUTE_NAMESPACE
		= "http://xmlmerge.el4j.elca.ch";
	
	/**
	 * Action attribute.
	 */
	public static final String ACTION_ATTRIBUTE = "action";

	/**
	 * Matcher attribute.
	 */
	public static final String MATCHER_ATTRIBUTE = "matcher";
	
	/**
	 * {@inheritDoc}
	 */
	public void configure(XmlMerge xmlMerge) throws ConfigurationException {

		MergeAction defaultMergeAction = new OrderedMergeAction();

		Mapper mapper = new NamespaceFilterMapper(ATTRIBUTE_NAMESPACE);

		defaultMergeAction.setMapperFactory(new StaticOperationFactory(mapper));

		// Configure the action factory
		OperationResolver actionResolver = new OperationResolver(
			StandardActions.class);

		defaultMergeAction.setActionFactory(new AttributeOperationFactory(
			defaultMergeAction, actionResolver, ACTION_ATTRIBUTE,
			ATTRIBUTE_NAMESPACE));

		// Configure the matcher factory
		Matcher defaultMatcher = new TagMatcher();

		OperationResolver matcherResolver = new OperationResolver(
			StandardMatchers.class);

		defaultMergeAction.setMatcherFactory(new AttributeOperationFactory(
			defaultMatcher, matcherResolver, MATCHER_ATTRIBUTE,
			ATTRIBUTE_NAMESPACE));

		xmlMerge.setRootMapper(mapper);
		xmlMerge.setRootMergeAction(defaultMergeAction);
	}

}
