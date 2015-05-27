define('ace/mode/song', function(require, exports, module) {

    var oop = require("ace/lib/oop");
    var TextMode = require("ace/mode/text").Mode;
    var Tokenizer = require("ace/tokenizer").Tokenizer;
    var SongHighlightRules = require("ace/mode/song_highlight_rules").SongHighlightRules;

    var Mode = function() {
        this.$tokenizer = new Tokenizer(new SongHighlightRules().getRules());
    };
    oop.inherits(Mode, TextMode);

    (function() {
        // Extra logic goes here. (see below)
    }).call(Mode.prototype);

    exports.Mode = Mode;
});

define('ace/mode/song_highlight_rules', function(require, exports, module) {

    var oop = require("ace/lib/oop");
    var TextHighlightRules = require("ace/mode/text_highlight_rules").TextHighlightRules;

    var SongHighlightRules = function() {

        this.$rules = {
            "start" : [
                {
                    token : "string", // single line
                    regex : '^[^:]*:'
                },
                {
                    token : "variable", // single line
                    regex : '^((\\s*[A-G](b|#)?(m|M|min|maj|dim|Δ|°|ø|Ø)?((sus|add)?(b|#)?(2|4|5|6|7|9|10|11|13)?)*(\\+|aug|alt)?(/[A-G](b|#)?)?\\s*)|(\\s*\\(x[0-9]+\\)\\s*))*$'
                }
            ]
        };

    }

    oop.inherits(SongHighlightRules, TextHighlightRules);

    exports.SongHighlightRules = SongHighlightRules;
});
