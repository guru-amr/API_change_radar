import client from './client';

// Auth
export const register = (data) => client.post('/auth/register', data);
export const login = (data) => client.post('/auth/login', data);

// Monitored APIs
export const getApis = () => client.get('/api/monitored-apis');
export const getApi = (id) => client.get(`/api/monitored-apis/${id}`);
export const createApi = (data) => client.post('/api/monitored-apis', data);
export const updateApi = (id, data) => client.put(`/api/monitored-apis/${id}`, data);
export const deleteApi = (id) => client.delete(`/api/monitored-apis/${id}`);

// Change History
export const getChanges = (apiId) => client.get(`/api/monitored-apis/${apiId}/changes`);
export const getChange = (apiId, reportId) => client.get(`/api/monitored-apis/${apiId}/changes/${reportId}`);
export const diffVersions = (apiId, fromVersion, toVersion) =>
  client.post(`/api/monitored-apis/${apiId}/changes/diff`, { fromVersion, toVersion });

// Alert Channels
export const getChannels = () => client.get('/api/alert-channels');
export const createChannel = (data) => client.post('/api/alert-channels', data);
export const deleteChannel = (id) => client.delete(`/api/alert-channels/${id}`);
