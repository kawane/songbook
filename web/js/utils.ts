
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

export interface OnSuccess {
    (data: string, req?: XMLHttpRequest): void;
}

export interface OnError {
    (error: string, req?: XMLHttpRequest): void;
}

export interface RequestData {
    method: string;
    url: string;
    headers: any;
    data?: any;
    onSuccess?: OnSuccess;
    onError?: OnError;
}

export function request(requestData: RequestData) {
    var req = new XMLHttpRequest();
    req.open(requestData.method, requestData.url, true);
    if (requestData.headers) {
        Object.keys(requestData.headers).forEach((headerName) => {
            req.setRequestHeader(headerName, requestData.headers[headerName]);
        });
    }
    req.onreadystatechange = () => {
        if (req.readyState == 4) {
            if (req.status < 300) {
                if (requestData.onSuccess) {
                    requestData.onSuccess(req.response, req);
                }
            } else {
                if (requestData.onError) {
                    requestData.onError(req.response, req);
                }
            }
        }
    };
    req.send(requestData.data);
}
