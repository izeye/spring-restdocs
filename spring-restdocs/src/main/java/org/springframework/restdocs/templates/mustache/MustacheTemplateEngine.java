/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.restdocs.templates.mustache;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.restdocs.mustache.Mustache;
import org.springframework.restdocs.mustache.Mustache.Compiler;
import org.springframework.restdocs.mustache.Template.Fragment;
import org.springframework.restdocs.templates.Template;
import org.springframework.restdocs.templates.TemplateEngine;
import org.springframework.restdocs.templates.TemplateResourceResolver;

/**
 * A <a href="https://mustache.github.io">Mustache</a>-based {@link TemplateEngine}
 * implemented using <a href="https://github.com/samskivert/jmustache">JMustache</a>.
 * <p>
 * Note that JMustache has been repackaged and embedded to prevent classpath conflicts.
 * 
 * @author Andy Wilkinson
 */
public class MustacheTemplateEngine implements TemplateEngine {

	private final Compiler compiler = Mustache.compiler().escapeHTML(false);

	private final TemplateResourceResolver templateResourceResolver;

	private final Map<String, Object> defaultContext = new HashMap<>();

	/**
	 * Creates a new {@link MustacheTemplateEngine} that will use the given
	 * {@code templateResourceResolver} to resolve template paths.
	 * 
	 * @param templateResourceResolver The resolve to use
	 */
	public MustacheTemplateEngine(TemplateResourceResolver templateResourceResolver) {
		this.templateResourceResolver = templateResourceResolver;

		this.defaultContext.put("codeBlock", new Mustache.Lambda() {

			@Override
			public void execute(Fragment frag, Writer out) throws IOException {
				StringWriter fragmentWriter = new StringWriter();
				frag.execute(fragmentWriter);
				Template codeBlock = MustacheTemplateEngine.this
						.compileTemplate("code-block");
				@SuppressWarnings("unchecked")
				Map<String, Object> context = (Map<String, Object>) frag.context();
				Map<String, Object> nestedContext = new HashMap<String, Object>();
				nestedContext.putAll(context);
				nestedContext.put("code", fragmentWriter.toString());
				out.append(codeBlock.render(nestedContext));
			}
		});
	}

	@Override
	public Template compileTemplate(String name) throws IOException {
		Resource templateResource = this.templateResourceResolver
				.resolveTemplateResource(name);
		return new MustacheTemplate(this.compiler.compile(new InputStreamReader(
				templateResource.getInputStream())), this.defaultContext);
	}

}
