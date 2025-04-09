## 결제 복구 서비스

> - 알수 없는 에러가 발생하여 결제 서비스가 종료되는 경우
> - PSP 일시적인 결제 오류 발생한 경우
> - 어플리케이션이 실제 배포되면서 결제 처리중인 어플리케이션이 죽는 경우 


### 결제 상태 
- 1. NOT_STARTED : 결제가 만들어진 초기 상태
- 2. EXECUTING : 결제 진행 중(PSP 인증 후 결제 승인 시작 단계)
- 3. SUCCESS : 결제 승인이 성공된 상태
- 4. FAILURE : 결제 승인 실패 상태
- 5. UNKNOWN : 결제 승인 상태를 알 수 없는 상태

결제 복구는 어떤 상태를 해야할지 ?
- EXECUTING : 결제 승인 요청 후 변하지 않는 상태이므로 결제 승인 여부를 알 수 없음
- UNKNOWN : 결제 승인 요청 후 변하지 않는 상태이므로 결제 승인 여부를 알 수 없음

### 결제 복구 Sequence diagram
1. Scheduling -> PaymentRecoveryUseCase
    - n분을 주기로 PaymentRecoveryUseCase를 호출
2. PaymentRecoveryUseCase -> LoadPendingPaymentPort
    - 결제 상태가 EXECUTING 또는 UNKNOWN인 결제 목록을 조회
3. PaymentRecoveryUseCase -> PaymentValidationPort 
    - 결제 유효성 검사를 진행한다.
4. PaymentRecoveryUseCase -> PSP
    - PSP에 결제 승인 요청을 한다.
5. PaymentRecoveryUseCase -> PaymentStatusUpdatePort
    - 결제 승인 결과를 결제 상태에 반영한다.


### 만약 결제에 계속 실패되는 결제?
- 재시도를 통해 해결되지 않는 결제에 경우 특정 횟수를 초과하게 되면 재시도로 해결되지 않았다고 하고,
수동 결제 및 알림 처리를 구현하면됨

## 결제 복구 서비스 고려사항

### Bulk Head Pattern
- 시스템의 신뢰성을 높이기 위해 사용되는 패턴
- 하나의 작업이 실패하더라도, 다른 작업에 영향을 주지 않게 함으로 써 신뢰성을 보장하는 패턴

> 각 작업의 Workload 마다 리소스를 분리함이 목적
> Service A가 작업에 문제가 생겨 Workload1 의 Connection Pool 리소스 소모
> Service B는 Workload2 의 Connection Pool 리소스를 사용하므로 영향을 받지 않음

### Scalability 고려하기
- 결제 시스템을 운영할 때 서버 단일 인스턴스를 사용하지 않음
- 확장성을 위해 여러 결제 서비스 인스턴스가 배포되는데 각 서비스가 동일한 Pending 상태의 결제 중복 처리 문제 발생 할 수 있음

> - 각 결제 서비스가 복구해야할 결제를 나눠서 처리하는 것이 중요함
> - 예를 들어 쿠버네티스를 사용하는 환경이라면 StatefulSet을 사용하여 각 결제 서비스 인스턴스가 복구해야할 결제를 나눠서 처리할 수 있음

### 병렬 처리
- WebFlux를 사용하여 결제 복구 서비스를 구현할 경우
API 외부 호출을 병렬로 처리하는 것이 성능에 유리하다. 즉, I/O 작업이 대기 상태에 있을 때 스레드가 블록되지 않고 다른 작업을 처리할 수 있고,
동시에 많은 수의 연결을 효율적으로 관리할 수 있고, 각 외부 API 연결에 대해 스레드를 차지하지 않으므로 성능이 향상된다.

- 병렬 처리가 가능한 대상인지, 동시성 문제가 없는지, 통신 대상 서버에 과도한 트래픽이 집중되지 않는지에 대해 고려해야한다.


