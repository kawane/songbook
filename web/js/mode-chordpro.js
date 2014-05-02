define(function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

var JsonHighlightRules = function() {

    // regexp must not have capturing parentheses. Use (?:) instead.
    // regexps are ordered -> the first match is used
    this.$rules = {
        "start" : [
            {
                token : "paren.lparen",
                regex : "\\[",
                next  : "chord"
            },
            {
                token : "paren.lparen",
                regex : "\\{",
                next  : "directive"
            } 
        ],
        "chord" : [
            {
                token : "variable",
                regex : '[A-Za-z0-9-_/]+'
            }, {
                token : "paren.rparen",
                regex : "\\]",
                next  : "start"
            }
        ],
        "directive" : [
            {
                token : "variable",
                regex : '([A-Za-z0-9-_/]+)(?=:)'
            },
            {
                token : "string",
                regex : '(?:)([A-Za-z0-9- _/]+)'
            }, {
                token : "paren.rparen",
                regex : "\\}",
                next  : "start"
            }
        ]
    };
    
};

oop.inherits(JsonHighlightRules, TextHighlightRules);

exports.JsonHighlightRules = JsonHighlightRules;
});
