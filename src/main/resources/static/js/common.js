// 1. 상태 관리 변수
        let isLoggedIn = sessionStorage.getItem("isLoggedIn") === "true"; // 브라우저 저장소(sessionStorage)를 활용해 새로고침 시에도 상태 유지
        let currentIdx = 1; // 현재 활성화된 페이지
        const maxPages = 10; // 전체 페이지 수
        let organizedDataGlobal = []; // 무드보드 구조화 데이터를 담을 전역 변수

        function checkAuthAndNavigate(pageIdx) {
        if (!isLoggedIn) {
            alert("로그인이 필요한 서비스입니다.");
            window.location.href = '/login'; // 🚀 백엔드 URL로 직접 이동
            return;
        }
        navigateTo(pageIdx);
    }


        // 2. 데이터 소스 구조 (핵심 데이터 저장소)

        // 브랜드기획 동기화 - 무드태그 선택지에 따라 하위 스타일을 분류하는 매핑 데이터
        const HIERARCHY_DATA_SOURCE = {
            ACTIVE: ["dynamic", "speed", "intense", "power", "race", "energy"],
            CALM: ["wellness", "soft", "mindful", "minimal", "balance", "relaxed"],
            PERFORMANCE: ["running", "training", "trail", "outdoor", "athlete", "sport"],
            LIFESTYLE: ["street", "fashion", "casual", "urban", "daily", "stylish"]
        };

        // 사용자의 선택 실시간으로 저장
        let selectedMainMoods = [];  // 사용자가 선택한 메인 무드
        let selectedSubKeywordsList = [];  // 사용자가 선택한 세부 키워드
        let currentLiveSessionAssets = ["도전 (Challenge)", "러닝 (Running)", "새벽기상 루틴", "도시 중심 (Urban)", "미니멀 (Minimal)", "퍼포먼스 러닝웨어", "Black + White", "통기성 메쉬 (Mesh)"]; // 현재 기획 중인 프로젝트 자산 리스트(지금은 화면 보여주기용이라 나중에 삭제)


        // 프로젝트 저장소 - 가상 데이터(지금은 화면 보여주기용이라 나중에 삭제)
        let virtualFolders = [
            { id: "all", name: "전체 프로젝트", isSystem: true },
            { id: "f1", name: "고프코어 라인업", isSystem: false },
            { id: "f2", name: "시티 트레일 아웃핏", isSystem: false }
        ];
        let currentSelectedFolderId = "all";


        // 실제 저장된 프로젝트 데이터 배열 (지금은 화면 보여주기용이라 나중에 삭제)
        let virtualProjects = [
            {
                id: "p1", folder: "f1", title: "도시형 새벽 러닝 가이드라인 보드셋", date: "2026-05-12",
                assets: ["도전 (Challenge)", "러닝 (Running)", "새벽기상 루틴", "도시 중심 (Urban)", "미니멀 (Minimal)", "퍼포먼스 러닝웨어", "Black + White", "통기성 메쉬 (Mesh)"],
                moodboardData: [],
                analysis: { benchmarks: [{ name: "On Running", score: 94 }] },
                selectedKeywords: ["Premium", "Urban Tech", "Running Sleek"]
            },
            {
                id: "p2", folder: "f1", title: "헤비 나일론 아우터 포지셔닝 구조도", date: "2026-05-20",
                assets: ["자유 (Freedom)", "트레일러닝 (Trail)", "도심 러닝 루틴", "트레일 (Trail) 코스", "하이 콘트라스트", "익스페디션 아웃도어", "Navy + Lime", "내마모 나일론 (Nylon)"],
                moodboardData: [],
                analysis: { benchmarks: [{ name: "Salomon", score: 91 }] },
                selectedKeywords: ["Heavy", "Nylon", "Outdoor"]
            },
            {
                id: "p3", folder: "f2", title: "시티 트레일 고성능 테크 팩 모델", date: "2026-06-02",
                assets: ["승리 (Victory)", "하이킹 (Hiking)", "리커버리 샤워", "산악 지대 (Mountain)", "에디토리얼 매거진", "테크니컬 트레이닝", "Earth Tone", "방수 투습 고어텍스"],
                moodboardData: [],
                analysis: { benchmarks: [{ name: "Patagonia", score: 95 }] },
                selectedKeywords: ["City Trail", "Tech-Pack", "Performance"]
            }
        ];

        // 페이지 로드 시 초기 화면 렌더링 (✨ 현재 주소창의 위치에 따라 내부 인덱스 동기화 설정)
        window.onload = function() {
            if (typeof renderFolderTree === "function") renderFolderTree();
            if (typeof renderProjectGallery === "function") renderProjectGallery();

            // 🚀 [소셜 로그인 자동 감지 센서]
            // 주소창에 ?socialLogin=true 가 붙어 들어왔다면 프론트엔드 로그인 상태도 동기화 시켜줍니다.
            const urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('socialLogin') === 'true') {
                sessionStorage.setItem("isLoggedIn", "true");
                isLoggedIn = true;
                // 주소창의 지저분한 파라미터(?socialLogin=true)를 사용자 모르게 지워주는 깔끔한 처리
                window.history.replaceState({}, document.title, window.location.pathname);
            }

            syncAuthUI();

            // 멀티 페이지 환경 대응: 현재 URL 경로에 맞게 내부 currentIdx 세팅
            const path = window.location.pathname;
            if (path === '/') currentIdx = 1;
            else if (path === '/login') currentIdx = 3;
            else if (path === '/signup') currentIdx = 4;
            else if (path === '/brandsync') {
                currentIdx = 6;
                // brandsync 페이지 내부의 탭(page6)을 자동으로 활성화
                const activePage = document.getElementById('page6');
                if (activePage) activePage.classList.add('active-view');
                const stepBar = document.getElementById('global-step-bar');
                if (stepBar) stepBar.style.display = 'flex';
                const activeStep = document.getElementById('st-6');
                if (activeStep) activeStep.classList.add('active');
            }
        };


        // 3. 페이지 네비게이션 및 제어

        // 로고 클릭 시 메인 페이지로 이동
        function handleLogoClick() { window.location.href = '/'; }

        // 워크스페이스 진입 시 로그인 여부 체크 (✨ 6번 탭이 아니라 /brandsync 주소로 진짜 이동!)
        function handleEntrance() {
            const 찐로그인상태 = sessionStorage.getItem("isLoggedIn") === "true";
            if (찐로그인상태) {
                window.location.href = '/brandsync'; // 🚀 이제 컨트롤러 주소로 실제 이동합니다!
            } else {
                alert("로그인이 필요한 워크스페이스입니다.");
                window.location.href = '/login'; // 🚀 로그인 페이지 주소로 이동합니다!
            }
        }

        // 페이지 이동 시 저장되지 않은 작업 방지 알림
        function handleNavInterruption(targetPageIdx) {
            if (currentIdx === 6 && !confirm("진행 중인 프로젝트가 저장되지 않습니다. 이동하시겠습니까?")) return;
            navigateTo(targetPageIdx);
        }


        // 페이지 전환 및 공통 제어 (✨ 멀티 페이지간 가교 역할 및 내부 탭 전환용)
        function navigateTo(idx) {
            // 다른 파일로 페이지 점프가 필요한 핵심 인덱스 분기 처리
            if (idx === 1) { window.location.href = '/'; return; }
            if (idx === 3) { window.location.href = '/login'; return; }
            if (idx === 6) { window.location.href = '/brandsync'; return; }

            // 현재 HTML 내부에 해당 page id 뷰가 존재할 때만 작동 (brandsync 내부 스텝 전환용)
            const activePage = document.getElementById(`page${idx}`);
            if (!activePage) return;

            currentIdx = idx;
            document.querySelectorAll('.page-view').forEach(p => p.classList.remove('active-view'));
            activePage.classList.add('active-view');

            // 스텝 바 제어 (6~9번 페이지만 노출)
            const stepBar = document.getElementById('global-step-bar');
            if (stepBar) {
                if (idx >= 6 && idx <= 9) {
                    stepBar.style.display = 'flex';
                    document.querySelectorAll('.step').forEach(s => s.classList.remove('active'));

                    const activeStep = document.getElementById(`st-${idx}`);
                    if (activeStep) {
                        activeStep.classList.add('active');
                    }
                } else {
                    stepBar.style.display = 'none';
                }
            }
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        // 이전 페이지 이동 함수
        function goBack() {
            if (currentIdx > 6) { // brandsync 내부 스텝 제어용
                navigateTo(currentIdx - 1);
            } else {
                window.location.href = '/'; // 그 외엔 메인으로
            }
        }


        // 4. 로그인/ 인증로직

        // 회원가입(사용가능한 이메일인지 확인하는 로직 추가)
        function checkEmailDuplicate() { alert("사용가능한 이메일 포맷입니다."); }

        // 로그인 (✨ 성공 시 메인으로 주소 이동)
        function executeLogin() {
            const emailInput = document.getElementById('login-email');
            const pwInput = document.getElementById('login-pw');

            const email = emailInput ? emailInput.value.trim() : "";
            const pw = pwInput ? pwInput.value.trim() : "";

            if (!email || !pw) {
                alert("⚠️ 이메일과 비밀번호를 모두 입력해주세요.");
                return;
            }

            fetch('/api/members/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email: email,
                    password: pw
                })
            })
            .then(async response => {
                if (response.ok) {
                    const msg = await response.text();
                    alert("🎉 " + msg);

                    sessionStorage.setItem("isLoggedIn", "true");
                    isLoggedIn = true;

                    syncAuthUI();
                    window.location.href = '/'; // 🚀 로그인 완료 후 실제 메인 화면 주소로 이동!
                } else {
                    const errorMsg = await response.text();
                    alert("❌ 로그인 실패: " + errorMsg);
                }
            })
            .catch(error => {
                console.error("통신 에러:", error);
                alert("🚨 서버 통신 중 오류가 발생했습니다.");
            });
        }

        // 회원가입 (✨ 가입 성공 시 로그인 주소로 이동)
        function executeRegister() {
            const name = document.getElementById('join-name').value.trim();
            const email = document.getElementById('join-email').value.trim();
            const pw = document.getElementById('join-pw').value.trim();
            const pwConfirm = document.getElementById('join-pw-confirm').value.trim();

            if (!name || !email || !pw || !pwConfirm) {
                alert("⚠️ 모든 항목을 입력해주세요.");
                return;
            }

            if (pw !== pwConfirm) {
                alert("⚠️ 비밀번호가 일치하지 않습니다.");
                return;
            }

            fetch('/api/members/join', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    nickname: name,
                    email: email,
                    password: pw
                })
            })
            .then(async response => {
                if (response.ok) {
                    const msg = await response.text();
                    alert("🎉 " + msg);
                    window.location.href = '/login'; // 🚀 가입 완료 후 로그인 주소로 실제 이동!
                } else {
                    const errorMsg = await response.text();
                    alert("❌ 가입 실패: " + errorMsg);
                }
            })
            .catch(error => {
                console.error("통신 에러:", error);
                alert("🚨 서버 통신 중 오류가 발생했습니다.");
            });
        }

        // 로그아웃 (✨ 저장소 데이터 삭제 후 메인 주소 이동)
        function triggerLogout() {
        if (confirm("로그아웃 하시겠습니까?")) {
            sessionStorage.removeItem("isLoggedIn");
            isLoggedIn = false;
            syncAuthUI();
            window.location.href = '/'; // 🚀 메인 페이지 주소로 아예 이동
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


        function executeModify() { alert("저장되었습니다."); window.location.href = '/'; }

        // 5. 브랜드기획 동기화 - 무드태그 선택 상반 비활성화 & 이미지 6개/12개 동적 연동
        function handleMoodTagClick(clickedTag, opposingTag) {
            const currentBtn = document.getElementById('main-tag-' + clickedTag);
            const oppoBtn = document.getElementById('main-tag-' + opposingTag);

            if(currentBtn.classList.contains('disabled-tag')) return;

            if(currentBtn.classList.contains('selected-tag')) {
                currentBtn.classList.remove('selected-tag');
                oppoBtn.classList.remove('disabled-tag');
                selectedMainMoods = selectedMainMoods.filter(t => t !== clickedTag);
            } else {
                currentBtn.classList.add('selected-tag');
                oppoBtn.classList.add('disabled-tag');
                oppoBtn.classList.remove('selected-tag');
                selectedMainMoods.push(clickedTag);
                const keywordsToClear = HIERARCHY_DATA_SOURCE[opposingTag];
                selectedSubKeywordsList = selectedSubKeywordsList.filter(item => !keywordsToClear.includes(item));
            }
            syncRenderQ4SubClusterGrid();
        }

        // 세부 키워드 그리드 생성
        function syncRenderQ4SubClusterGrid() {
            const poolContainer = document.getElementById('q4-dynamic-style-pool');
            if (!poolContainer) return; // 해당 엘리먼트가 없는 페이지라면 스킵
            poolContainer.innerHTML = "";

            if (selectedMainMoods.length === 0) {
                poolContainer.innerHTML = `<div style="grid-column: span 4; text-align: center; color: var(--text-gray); padding: 30px; font-size: 13px;">Q3 무드태그를 선택하시면 상응하는 하위 계층 핵심 이미지셋이 실시간으로 여기에 전개됩니다.</div>`;
                return;
            }

            selectedMainMoods.forEach(clusterKey => {
                const subItems = HIERARCHY_DATA_SOURCE[clusterKey];
                subItems.forEach(keywordValue => {
                    const isChosen = selectedSubKeywordsList.includes(keywordValue);
                    const card = document.createElement('div');
                    card.className = `dynamic-style-card ${isChosen ? 'chosen-style' : ''}`;
                    card.setAttribute('data-keyword', keywordValue);

                    card.innerHTML = `
                        <div class="card-img-placeholder">[PREVIEW: ${clusterKey}_${keywordValue}]</div>
                        <div style="text-align:center; font-weight:700; font-size:13px;">${keywordValue}</div>
                        <div style="text-align:center; font-size:10px; color:var(--text-gray); margin-top:2px;">${clusterKey} 계층 소속</div>
                    `;
                    card.onclick = function() { handleSubCardSelection(this, keywordValue); };
                    poolContainer.appendChild(card);
                });
            });
            enforceQ4LockingLimit();
        }

        // 브랜드 기획 동기화 - Q1~Q4 모두 입력 후 다음 단계 이동 가능
        function checkInputsAndNavigate() {
              const q1 = document.getElementById('brand-line-summary').value.trim();
              const q2 = document.getElementById('brand-object-search').value.trim();

              if (q1 === "" || q2 === "") {
                  alert("⚠️ Q1과 Q2의 내용을 모두 입력해주세요.");
                  return;
              }

              if (selectedMainMoods.length === 0) {
                  alert("⚠️ Q3에서 최소 1개의 브랜드 무드태그를 선택해주세요.");
                  return;
              }

              if (selectedSubKeywordsList.length === 0) {
                  alert("⚠️ Q4에서 최소 1개의 세부 스타일 키워드를 선택해주세요.");
                  return;
              }

              // 같은 brandsync 파일 안에서 7번 스텝으로 전환하므로 기존 navigateTo 연동 작동
              navigateTo(7);
          }

        // 스타일 선택 및 4개 제한 로직
        function handleSubCardSelection(cardElement, keywordValue) {
            if (cardElement.classList.contains('disabled-style')) return;

            if (cardElement.classList.contains('chosen-style')) {
                cardElement.classList.remove('chosen-style');
                selectedSubKeywordsList = selectedSubKeywordsList.filter(k => k !== keywordValue);
            } else {
                if (selectedSubKeywordsList.length > 4) {
                    alert("⚠️ 세부 스타일 지표 키워드는 최대 4개까지만 복합 바인딩이 허용됩니다.");
                    return;
                }
                cardElement.classList.add('chosen-style');
                selectedSubKeywordsList.push(keywordValue);
            }
            enforceQ4LockingLimit();
        }

        function enforceQ4LockingLimit() {
            const cards = document.querySelectorAll('.dynamic-style-card');
            if (selectedSubKeywordsList.length >= 4) {
                cards.forEach(card => {
                    const kw = card.getAttribute('data-keyword');
                    if (!selectedSubKeywordsList.includes(kw)) card.classList.add('disabled-style');
                });
            } else {
                cards.forEach(card => card.classList.remove('disabled-style'));
            }
        }


        // 레퍼런스 이미지 선택 - 중복선택, 최대 4개 제한
        function selectSingleReference(card, rowId) {
            const rowContainer = document.getElementById(rowId);
            if (!rowContainer) return;

            if (card.classList.contains('chosen')) {
                card.classList.remove('chosen');
                return;
            }

            const selectedCountInThisRow = rowContainer.querySelectorAll('.scroll-card.chosen').length;

            if (selectedCountInThisRow >= 4) {
                alert("⚠️ 한 카테고리당 최대 4개까지만 선택할 수 있습니다.");
                return;
            }

            card.classList.add('chosen');
        }