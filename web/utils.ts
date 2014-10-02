
export interface QueryParam {
    [s:string]: string;
}

export function getQueryParam(): QueryParam {
    var queryMap: QueryParam = {};
    if (location.search.length > 0) {
        location.search.substring(1)
            .split("&")
            .map((arg => arg.split("=")))
            .forEach((arg) => queryMap[arg[0]] = arg[1]);
    }
    return queryMap;
}


