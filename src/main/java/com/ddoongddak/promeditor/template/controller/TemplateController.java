package com.ddoongddak.promeditor.template.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/templates")
public class TemplateController {

    @GetMapping
    public String list() {
        return "template/list";
    }

    @GetMapping("/new")
    public String newTemplate() {
        return "template/edit";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id) {
        return "template/edit";
    }

    @GetMapping("/{id}/use")
    public String use(@PathVariable Long id) {
        return "template/use";
    }
}
