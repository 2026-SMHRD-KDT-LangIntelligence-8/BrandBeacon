
        // 1. 상태 관리 변수
        let isLoggedIn = false; // 로그인 여부
        let currentIdx = 1; // 현재 활성화된 페이지
        const maxPages = 10; // 전체 페이지 수
        let organizedDataGlobal = []; // 무드보드 구조화 데이터를 담을 전역 변수

        function checkAuthAndNavigate(pageIdx) {
        if (!isLoggedIn) {
            alert("로그인이 필요한 서비스입니다.");
            navigateTo(3); // 로그인 페이지(page3)로 이동
            return;
        }
        navigateTo(pageIdx);
    }

        // 로고 클릭 시 메인 페이지로 이동
        function handleLogoClick() { navigateTo(1); }

        // 워크스페이스 진입 시 로그인 여부 체크
        function handleEntrance() {
            if (isLoggedIn) {
                navigateTo(6); // 로그인 상태면 기획 시작 페이지(6번)로
            } else {
                alert("로그인이 필요한 워크스페이스입니다.");
                navigateTo(3); // 비로그인이면 로그인 페이지(3번)로
            }
        }

        // 페이지 이동 시 저장되지 않은 작업 방지 알림
        function handleNavInterruption(targetPageIdx) {
            if (currentIdx === 6 && !confirm("진행 중인 프로젝트가 저장되지 않습니다. 이동하시겠습니까?")) return;
            navigateTo(targetPageIdx);
        }

        // 페이지 전환 및 공통 제어
        function navigateTo(idx) {
            currentIdx = idx;

            // 모든 페이지 숨김 후 해당 페이지 활성화
            document.querySelectorAll('.page-view').forEach(p => p.classList.remove('active-view'));
            const activePage = document.getElementById(`page${idx}`);
            if (activePage) activePage.classList.add('active-view');

            // 스텝 바 제어 (6~9번 페이지만 노출)
            const stepBar = document.getElementById('global-step-bar');
            if (idx >= 6 && idx <= 9) {
                stepBar.style.display = 'flex';
                document.querySelectorAll('.step').forEach(s => s.classList.remove('active'));

                // 해당 ID가 존재할 때만 클래스를 추가하여 에러 방지
                const activeStep = document.getElementById(`st-${idx}`);
                if (activeStep) {
                    activeStep.classList.add('active');
                }
            } else {
                stepBar.style.display = 'none';
            }
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        // 이전 페이지 이동 함수
        function goBack() {
            if (currentIdx > 1) {
                navigateTo(currentIdx - 1);
            } else {
                alert("첫 번째 페이지입니다.");
            }
        }

        // 4. 로그인/ 인증로직
        // 회원가입(사용가능한 이메일인지 확인하는 로직 추가)
        function checkEmailDuplicate() { alert("사용가능한 이메일 포맷입니다."); }

        // 로그인
        function executeLogin() {
          isLoggedIn = true;
          syncAuthUI(); // 헤더 업데이트
          alert("반갑습니다. 워크스페이스 콘솔을 가동합니다.");
          navigateTo(1); // 로그인 후 메인 페이지(1번) 이동
      }

        // 회원가입
        function executeRegister() {
            alert("성공적으로 등록되었습니다.");
            isLoggedIn = true;
            syncAuthUI();
            navigateTo(1); // 가입 후 메인 페이지(1번)로 이동
        }

        // 로그아웃
        function triggerLogout() {
        if (confirm("로그아웃 하시겠습니까?")) {
            isLoggedIn = false;
            syncAuthUI();
            navigateTo(1); // 로그아웃 후 랜딩/메인(1번)으로 이동
        }
    }

        // UI 동기화 (로그인 상태에 따라 로그인 버튼 표시 제어)
        function syncAuthUI() {
            const loginMenu = document.getElementById('login-nav-menu');

            if (isLoggedIn) {
                if (loginMenu) {
                    loginMenu.style.display = "none";
                }
            } else {
                if (loginMenu) {
                    loginMenu.style.display = "list-item";
                }
            }
        }
        function executeModify() { alert("저장되었습니다."); navigateTo(1); }