import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 5 },
    { duration: '60s', target: 10 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],      // 탐색 단계에선 5% 허용(임시)
    http_req_duration: ['p(95)<120000'], // 120s
  },
};

const TARGET = __ENV.TARGET || 'http://host.docker.internal:8082';
const PRODUCT_URL = encodeURIComponent('fixture:musinsa-4121422');

export default function () {
  const res = http.get(
      `${TARGET}/api/clothes/extractions?url=${PRODUCT_URL}`,
      { timeout: '120s' } // ★ 120초로 상향
  );
  check(res, { 'status is 200': r => r.status === 200 });
  sleep(1);
}
