package com.github.dreamhead.moco;

import org.apache.http.client.HttpResponseException;
import org.junit.Test;

import java.io.IOException;

import static com.github.dreamhead.moco.Moco.*;
import static com.github.dreamhead.moco.MocoMount.to;
import static com.github.dreamhead.moco.Runner.running;
import static com.github.dreamhead.moco.helper.RemoteTestUtils.port;
import static com.github.dreamhead.moco.helper.RemoteTestUtils.remoteUrl;
import static com.github.dreamhead.moco.helper.RemoteTestUtils.root;
import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MocoGlobalRequestTest extends AbstractMocoHttpTest {
    @Test
    public void should_match_global_header() throws Exception {
        server = httpserver(port(), request(eq(header("foo"), "bar")));
        server.request(by(uri("/global-request"))).response(text("blah"));

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                String result = helper.getWithHeader(remoteUrl("/global-request"), of("foo", "bar"));
                assertThat(result, is("blah"));
            }
        });
    }

    @Test(expected = HttpResponseException.class)
    public void should_throw_exception_without_global_matcher() throws Exception {
        server = httpserver(port(), request(eq(header("foo"), "bar")));
        server.request(by(uri("/global-request"))).response(text("blah"));

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                String result = helper.get(remoteUrl("/global-request"));
                assertThat(result, is("blah"));
            }
        });
    }

    @Test
    public void should_match_global_header_with_any_response() throws Exception {
        server = httpserver(port(), request(eq(header("foo"), "bar")));
        server.response(text("blah"));

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                String result = helper.getWithHeader(root(), of("foo", "bar"));
                assertThat(result, is("blah"));

            }
        });
    }

    @Test(expected = HttpResponseException.class)
    public void should_throw_exception_without_global_matcher_for_any_response() throws Exception {
        server = httpserver(port(), request(eq(header("foo"), "bar")));
        server.response(text("blah"));

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                helper.get(root());
            }
        });
    }

    @Test
    public void should_match_with_exist_header() throws Exception {
        server = httpserver(port(), request(eq(header("foo"), "bar")));
        server.request(exist(header("blah"))).response(text("header"));

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                assertThat(helper.getWithHeader(root(), of("foo", "bar", "blah", "any")), is("header"));
            }
        });
    }

    @Test(expected = HttpResponseException.class)
    public void should_throw_exception_without_global_matcher_for_exist() throws Exception {
        server = httpserver(port(), request(eq(header("foo"), "bar")));
        server.request(exist(header("blah"))).response(text("header"));

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                helper.getWithHeader(root(), of("blah", "any"));
            }
        });
    }

    @Test
    public void should_match_with_json() throws Exception {
        server = httpserver(port(), request(by(uri("/path"))));
        final String jsonContent = "{\"foo\":\"bar\"}";
        server.request(json(text(jsonContent))).response("foo");
        running(server, new Runnable() {
            @Override
            public void run() throws IOException {
                assertThat(helper.postContent(remoteUrl("/path"), jsonContent), is("foo"));
            }
        });
    }

    @Test(expected = HttpResponseException.class)
    public void should_throw_exception_without_match_json() throws Exception {
        server = httpserver(port(), request(by(uri("/path"))));
        final String jsonContent = "{\"foo\":\"bar\"}";
        server.request(json(text(jsonContent))).response("foo");
        running(server, new Runnable() {
            @Override
            public void run() throws IOException {
                helper.postContent(root(), jsonContent);
            }
        });
    }

    @Test
    public void should_match_with_xml() throws Exception {
        server = httpserver(port(), request(by(uri("/path"))));
        server.request(xml(text("<request><parameters><id>1</id></parameters></request>"))).response("foo");
        running(server, new Runnable() {
            @Override
            public void run() throws IOException {
                assertThat(helper.postFile(remoteUrl("/path"), "foo.xml"), is("foo"));
            }
        });
    }

    @Test(expected = HttpResponseException.class)
    public void should_throw_exception_without_match_xml() throws Exception {
        server = httpserver(port(), request(by(uri("/path"))));
        server.request(xml(text("<request><parameters><id>1</id></parameters></request>"))).response("foo");
        running(server, new Runnable() {
            @Override
            public void run() throws IOException {
                helper.postFile(root(), "foo.xml");
            }
        });
    }

    @Test
    public void should_match_mount() throws Exception {
        final String MOUNT_DIR = "src/test/resources/test";
        server = httpserver(port(), request(eq(header("foo"), "bar")));
        server.mount(MOUNT_DIR, to("/dir"));

        running(server, new Runnable() {
            @Override
            public void run() throws IOException {
                assertThat(helper.getWithHeader(remoteUrl("/dir/dir.response"), of("foo", "bar")), is("response from dir"));
            }
        });
    }

    @Test(expected = HttpResponseException.class)
    public void should_throw_exception_without_match_mount() throws Exception {
        final String MOUNT_DIR = "src/test/resources/test";
        server = httpserver(port(), request(eq(header("foo"), "bar")));
        server.mount(MOUNT_DIR, to("/dir"));

        running(server, new Runnable() {
            @Override
            public void run() throws IOException {
                assertThat(helper.get(remoteUrl("/dir/dir.response")), is("response from dir"));
            }
        });
    }

    @Test
    public void should_match_request_based_on_not_matcher() throws Exception {
        server = httpserver(port(), request(eq(header("foo"), "bar")));
        server.request(not(by(uri("/foo")))).response(text("bar"));

        running(server, new Runnable() {
            @Override
            public void run() throws IOException {
                assertThat(helper.getWithHeader(remoteUrl("/bar"), of("foo", "bar")), is("bar"));
            }
        });
    }

    @Test(expected = HttpResponseException.class)
    public void should_throw_exception_without_match_not() throws Exception {
        server = httpserver(port(), request(eq(header("foo"), "bar")));
        server.request(not(by(uri("/foo")))).response(text("bar"));

        running(server, new Runnable() {
            @Override
            public void run() throws IOException {
                helper.get(remoteUrl("/bar"));
            }
        });
    }
}
