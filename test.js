import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 5 },
    { duration: '60s', target: 10 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<30000'],
  },
};

const TARGET = __ENV.TARGET || 'http://host.docker.internal:8080';
const PRODUCT_URL = encodeURIComponent('fixture:musinsa-4121422');

export default function () {
  const res = http.get(`${TARGET}/api/clothes/extractions?url=${PRODUCT_URL}`, { timeout: '60s' });
  check(res, { 'status is 200': r => r.status === 200 });
  sleep(1);
}
