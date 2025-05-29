export const API_BASE_URL = 'https://www.tmmtmm.online/api';
export const ACCESS_TOKEN = 'accessToken';
// export const OAUTH2_REDIRECT_URI = 'https://www.tmmtmm.online/api/oauth2/redirect'
export const OAUTH2_REDIRECT_URI = 'https://www.tmmtmm.online/api/oauth2/callback'


export const GOOGLE_AUTH_URL = API_BASE_URL + '/oauth2/authorize/google?redirect_uri=' + OAUTH2_REDIRECT_URI;
// export const GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/userinfo.email&include_granted_scopes=true&response_type=token&state=3EAB37D9D5310BFE&redirect_uri=https://www.tmmtmm.online/api/oauth2/redirect/google&client_id=36933357862-4308uegej069gs4ggmflaga95erg0627.apps.googleusercontent.com";
export const FACEBOOK_AUTH_URL = API_BASE_URL + '/oauth2/authorize/facebook?redirect_uri=' + OAUTH2_REDIRECT_URI;
export const GITHUB_AUTH_URL = API_BASE_URL + '/oauth2/authorize/github?redirect_uri=' + OAUTH2_REDIRECT_URI;
