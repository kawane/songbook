define(["require", "exports"], function (require, exports) {
    var ReaderParserInput = (function () {
        function ReaderParserInput(data) {
            this.data = data;
            this.position = 0;
        }
        ReaderParserInput.prototype.peek = function (index) {
            var i = this.position + index;
            if (i < this.data.length) {
                return this.data.charAt(i);
            }
            else {
                return ReaderParserInput.EOT;
            }
        };
        ReaderParserInput.prototype.peekString = function (beginIndex, endIndex) {
            if (endIndex < this.data.length) {
                return this.data.substring(this.position + beginIndex, this.position + endIndex);
            }
            else {
                return this.data.substring(this.position + beginIndex);
            }
        };
        ReaderParserInput.prototype.skip = function (n) {
            this.position += n;
            return n;
        };
        ReaderParserInput.prototype.getPosition = function () {
            return this.position;
        };
        ReaderParserInput.prototype.toString = function () {
            return "ReaderParserInput pos: " + this.position + " data: " + this.data;
        };
        ReaderParserInput.EOT = "\u0000";
        return ReaderParserInput;
    })();
    return ReaderParserInput;
});
//# sourceMappingURL=ReaderParserInput.js.map