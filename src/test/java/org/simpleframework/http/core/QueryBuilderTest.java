package org.simpleframework.http.core;

import org.simpleframework.http.Query;
import org.simpleframework.http.message.MockBody;
import org.simpleframework.http.message.MockHeader;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class QueryBuilderTest{

    @Test
    public void testBuilder() throws Exception {
        final MockRequest request = new MockRequest();

        request.setContentType("application/x-www-form-urlencoded");
        request.setContent("a=post_A&c=post_C&e=post_E");

        final MockBody body = new MockBody();
        final MockHeader header = new MockHeader("/path?a=query_A&b=query_B&c=query_C&d=query_D");
        final MockEntity entity = new MockEntity(body, header);
        final QueryBuilder builder = new QueryBuilder(request, entity);

        final Query form = builder.build();

        AssertJUnit.assertEquals(form.getAll("a").size(), 2);
        AssertJUnit.assertEquals(form.getAll("b").size(), 1);
        AssertJUnit.assertEquals(form.getAll("c").size(), 2);
        AssertJUnit.assertEquals(form.getAll("e").size(), 1);

        AssertJUnit.assertEquals(form.get("a"), "query_A");
        AssertJUnit.assertEquals(form.get("b"), "query_B");
        AssertJUnit.assertEquals(form.get("c"), "query_C");
        AssertJUnit.assertEquals(form.get("e"), "post_E");
    }

}
