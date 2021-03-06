/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.dbcommon;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.filter.*;

/**
 * DatabaseFilterTranslator abstract class translate filters to database WHERE
 * clause The resource specific getAttributeName must be provided in real
 * translator.
 *
 * @since 1.0
 */
public abstract class DatabaseFilterTranslator extends AbstractFilterTranslator<FilterWhereBuilder> {

    protected final ObjectClass oclass;
    protected final OperationOptions options;

    /**
     * DatabaseFilterTranslator translate filters to database WHERE clause.
     *
     * @param oclass
     *            the object class
     * @param options
     *            the filter options
     */
    public DatabaseFilterTranslator(final ObjectClass oclass, final OperationOptions options) {
        this.oclass = oclass;
        this.options = options;
    }

    /**
     * @return
     */
    protected FilterWhereBuilder createBuilder() {
        return new FilterWhereBuilder();
    }

    @Override
    protected FilterWhereBuilder createAndExpression(FilterWhereBuilder leftExpression,
            FilterWhereBuilder rightExpression) {
        FilterWhereBuilder build = createBuilder();
        build.join("AND", leftExpression, rightExpression);
        return build;
    }

    @Override
    protected FilterWhereBuilder createOrExpression(FilterWhereBuilder leftExpression,
            FilterWhereBuilder rightExpression) {
        FilterWhereBuilder build = createBuilder();
        build.join("OR", leftExpression, rightExpression);
        return build;
    }

    @Override
    protected FilterWhereBuilder createEqualsExpression(EqualsFilter filter, boolean not) {
        final Attribute attribute = filter.getAttribute();
        if (!validateSearchAttribute(attribute)) {
            return null;
        }

        SQLParam param = getSQLParam(attribute, oclass, options);
        if (param == null) {
            return null;
        }
        final FilterWhereBuilder ret = createBuilder();
        if (not) {
            ret.getWhere().append("NOT ");
        }
        // Normalize NULLs
        if (param.getValue() == null) {
            ret.addNull(param.getName());
            return ret;
        }
        ret.addBind(param, "=");
        return ret;
    }

    @Override
    protected FilterWhereBuilder createContainsExpression(ContainsFilter filter, boolean not) {
        final Attribute attribute = filter.getAttribute();
        if (!validateSearchAttribute(attribute)) {
            return null;
        }

        SQLParam param = getSQLParam(attribute, oclass, options);
        if (param == null || param.getValue() == null || !(param.getValue() instanceof String)) {
            // Null value filter is not supported
            return null;
        }

        String value = (String) param.getValue();

        final FilterWhereBuilder ret = createBuilder();
        if (not) {
            ret.getWhere().append("NOT ");
        }
        // To be sure, this is not already quoted
        if (!value.startsWith("%")) {
            value = "%" + value;
        }
        if (!value.endsWith("%")) {
            value = value + "%";
        }
        ret.addBind(new SQLParam(param.getName(), value, param.getSqlType()), "LIKE");
        return ret;
    }

    @Override
    protected FilterWhereBuilder createEndsWithExpression(EndsWithFilter filter, boolean not) {
        final Attribute attribute = filter.getAttribute();
        if (!validateSearchAttribute(attribute)) {
            return null;
        }

        SQLParam param = getSQLParam(attribute, oclass, options);
        if (param == null || param.getValue() == null || !(param.getValue() instanceof String)) {
            // Null value filter is not supported
            return null;
        }

        String value = (String) param.getValue();

        final FilterWhereBuilder ret = createBuilder();
        if (not) {
            ret.getWhere().append("NOT ");
        }
        // To be sure, this is not already quoted
        if (!value.startsWith("%")) {
            value = "%" + value;
        }
        ret.addBind(new SQLParam(param.getName(), value, param.getSqlType()), "LIKE");
        return ret;
    }

    @Override
    protected FilterWhereBuilder createStartsWithExpression(StartsWithFilter filter, boolean not) {
        final Attribute attribute = filter.getAttribute();
        if (!validateSearchAttribute(attribute)) {
            return null;
        }

        SQLParam param = getSQLParam(attribute, oclass, options);
        if (param == null || param.getValue() == null || !(param.getValue() instanceof String)) {
            // Null value filter is not supported
            return null;
        }

        String value = (String) param.getValue();

        final FilterWhereBuilder ret = createBuilder();
        if (not) {
            ret.getWhere().append("NOT ");
        }
        // To be sure, this is not already quoted
        if (!value.endsWith("%")) {
            value = value + "%";
        }
        ret.addBind(new SQLParam(param.getName(), value, param.getSqlType()), "LIKE");
        return ret;
    }

    @Override
    protected FilterWhereBuilder createGreaterThanExpression(GreaterThanFilter filter, boolean not) {
        final Attribute attribute = filter.getAttribute();
        if (!validateSearchAttribute(attribute)) {
            return null;
        }

        SQLParam param = getSQLParam(attribute, oclass, options);
        if (param == null || param.getValue() == null) {
            return null;
        }
        final FilterWhereBuilder ret = createBuilder();
        final String op = not ? "<=" : ">";
        ret.addBind(param, op);
        return ret;
    }

    @Override
    protected FilterWhereBuilder createGreaterThanOrEqualExpression(
            GreaterThanOrEqualFilter filter, boolean not) {
        final Attribute attribute = filter.getAttribute();
        if (!validateSearchAttribute(attribute)) {
            return null;
        }

        SQLParam param = getSQLParam(attribute, oclass, options);
        if (param == null || param.getValue() == null) {
            return null;
        }

        final FilterWhereBuilder ret = createBuilder();
        final String op = not ? "<" : ">=";
        ret.addBind(param, op);
        return ret;
    }

    @Override
    protected FilterWhereBuilder createLessThanExpression(LessThanFilter filter, boolean not) {
        final Attribute attribute = filter.getAttribute();
        if (!validateSearchAttribute(attribute)) {
            return null;
        }

        SQLParam param = getSQLParam(attribute, oclass, options);
        if (param == null || param.getValue() == null) {
            return null;
        }

        final FilterWhereBuilder ret = createBuilder();
        final String op = not ? ">=" : "<";
        ret.addBind(param, op);
        return ret;
    }

    @Override
    protected FilterWhereBuilder createLessThanOrEqualExpression(LessThanOrEqualFilter filter,
            boolean not) {
        final Attribute attribute = filter.getAttribute();
        if (!validateSearchAttribute(attribute)) {
            return null;
        }

        SQLParam param = getSQLParam(attribute, oclass, options);
        if (param == null || param.getValue() == null) {
            return null;
        }

        final FilterWhereBuilder ret = createBuilder();
        final String op = not ? ">" : "<=";
        ret.addBind(param, op);
        return ret;
    }

    /**
     * Get the SQLParam for given attribute.
     *
     * @param attribute
     *            to translate
     * @return the expected SQLParam, or null if filter not supported
     *         {@link java.sql.Types}
     */
    protected abstract SQLParam getSQLParam(Attribute attribute, ObjectClass objectClass,
            OperationOptions options);

    /**
     * Validate the attribute to supported search types.
     *
     * @param attribute
     */
    protected boolean validateSearchAttribute(final Attribute attribute) {
        // Ignore streamed ( byte[] objects ) from query
        if (byte[].class.equals(AttributeUtil.getSingleValue(attribute).getClass())) {
            return false;
        }
        // Otherwise let the database process
        return true;
    }
}
