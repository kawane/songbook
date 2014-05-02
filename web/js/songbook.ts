
module songbook {
    export class SongItem {
        id: string;
        title: string;
        authors: string[];
        album: string;
    }

    export function displaySongItems(songItems: SongItem[] ): HTMLElement {
        var html = document.createElement("div");
        html.className="list-group";
        songItems.forEach(
            songItem => html.appendChild(displaySongItem(songItem))
        )
        return html;
    }

    export function displaySongItem(songItem: SongItem): HTMLElement {
        var a = document.createElement("a");
        a.className = "list-group-item";
        a.href = "/rest/song/" + songItem.id;

        var h4 = document.createElement("h4");
        h4.tagName = "h4";
        h4.className = "list-group-item-heading";
        h4.innerText = songItem.title;
        a.appendChild(h4);

        if( songItem.authors != null || songItem.album != null ) {
            var p = document.createElement("p");
            p.className = "list-group-item-text";
            p.innerText = "";
            if ( songItem.authors != null && songItem.authors.length > 0 ) {
                p.innerText += songItem.authors.join(', ');
            }
            if ( songItem.album != null ) {
                p.innerText += "(" + songItem.album + ")";
            }
            a.appendChild(p);
        }

        return a;
    }

    export function retrieveAndListSongs(targetId: string) {
        var req = new XMLHttpRequest();
        req.open('GET', '/rest/song', true);
        req.onreadystatechange = function() {
            if (req.readyState == 4 && req.status == 200) {
                // transforms JSON to HTML
                var text = this.responseText;
                if ( text.length == 0 ) return;

                var html = songbook.displaySongItems(JSON.parse(text));

                // clears current song list and appends received one
                var songList = document.getElementById(targetId);
                while (songList.firstChild != null ) songList.removeChild(songList.firstChild);
                songList.appendChild(html);
            }
        };
        req.send();
    }
}
