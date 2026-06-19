// 페이지 전환 및 공통 제어 (✨ 멀티 페이지간 가교 역할 및 내부 탭 전환용)
function navigateTo(idx) {
    // 🚀 [최상단 배치] 다른 파일로 페이지 점프가 필요한 핵심 인덱스는 DOM 검사 전에 즉시 이동 처리!
    if (idx === 1) { window.location.href = '/'; return; }
    if (idx === 3) { window.location.href = '/login'; return; }
    if (idx === 6) { window.location.href = '/brandsync'; return; }
    if (idx === 7) { window.location.href = '/reference'; return; }
    if (idx === 8) { window.location.href = '/moodboard'; return; }
    if (idx === 9) { window.location.href = '/dashboard'; return; } // 🚀 무드보드 -> 대시보드 주소 안내 추가!

    // 현재 HTML 내부에 해당 page id 뷰가 존재할 때만 작동 (동일 페이지 내 내부 스텝 전환용)
    const activePage = document.getElementById(`page${idx}`);
    if (!activePage) return; // 💡 다른 페이지 주소일 때는 이 장벽에 걸리기 전에 위에서 리턴됩니다.

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

        // 🚀 헤더 메뉴용: 로그인 체크 및 이동 함수
        function checkLoginOrRedirect(targetUrl) {
            // 1. 현재 로그인 상태 확인 (파일 상단에 정의된 isLoggedIn 변수 활용)
            if (!isLoggedIn) {
                alert("로그인 후 이용 가능한 서비스입니다.");
                window.location.href = '/login';
            } else {
                // 2. 로그인 상태면 해당 페이지로 이동
                window.location.href = targetUrl;
            }
        }

        // 현재 선택된 폴더 ID (null = 전체 프로젝트)
        let _currentFolderId = null;

        // 오른쪽 타이틀 영역 업데이트 (연필 아이콘 포함)
        function _setFolderTitle(name, folderId) {
            const el = document.getElementById('current-folder-title-display');
            if (!el) return;
            el.textContent = name;

            // 기존 연필 아이콘 제거
            const prev = document.getElementById('_folder-edit-btn');
            if (prev) prev.remove();

            if (folderId == null) return; // 전체 프로젝트는 아이콘 없음

            const btn = document.createElement('span');
            btn.id = '_folder-edit-btn';
            btn.title = '폴더명 수정';
            btn.textContent = '✏️';
            btn.style.cssText = 'margin-left:8px; font-size:13px; opacity:0.45; cursor:pointer; user-select:none; vertical-align:middle;';
            btn.onmouseenter = () => { btn.style.opacity = '0.85'; };
            btn.onmouseleave = () => { btn.style.opacity = '0.45'; };
            btn.onclick = () => {
                const current = el.textContent;
                const input = document.createElement('input');
                input.value = current;
                input.style.cssText = 'font-size:14px; color:var(--text-white); background:var(--bg-card-inner); border:1px solid var(--primary-orange); border-radius:4px; padding:2px 8px; outline:none; width:180px;';

                el.replaceWith(input);
                btn.remove();
                input.focus();
                input.select();

                function saveRename() {
                    const newName = input.value.trim();
                    if (!newName || newName === current) {
                        input.replaceWith(el);
                        _setFolderTitle(current, folderId);
                        return;
                    }
                    fetch(`/api/folders/${folderId}`, {
                        method: 'PATCH',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ folderName: newName })
                    }).then(res => {
                        if (res.ok) {
                            input.replaceWith(el);
                            _setFolderTitle(newName, folderId);
                            renderFolderTree();
                        } else {
                            res.text().then(t => { alert('수정 실패: ' + t); input.replaceWith(el); _setFolderTitle(current, folderId); });
                        }
                    });
                }

                input.addEventListener('keydown', e => {
                    if (e.key === 'Enter') saveRename();
                    if (e.key === 'Escape') { input.replaceWith(el); _setFolderTitle(current, folderId); }
                });
                input.addEventListener('blur', saveRename);
            };

            el.after(btn);
        }

        // 폴더 트리 렌더링
        function renderFolderTree() {
            const tree = document.getElementById('system-folder-tree-element');
            if (!tree) return;

            Promise.all([
                fetch('/api/folders/my').then(r => r.ok ? r.json() : []),
                fetch('/api/projects/my').then(r => r.ok ? r.json() : [])
            ]).then(([folders, projects]) => {
                tree.innerHTML = '';

                function _makeDrop(li, targetFolderId) {
                    li.addEventListener('dragover', e => {
                        e.preventDefault();
                        li.style.background = 'var(--selected-bg)';
                        li.style.borderColor = 'var(--primary-orange)';
                    });
                    li.addEventListener('dragleave', () => {
                        li.style.background = '';
                        li.style.borderColor = '';
                    });
                    li.addEventListener('drop', e => {
                        e.preventDefault();
                        li.style.background = '';
                        li.style.borderColor = '';
                        const projectId = e.dataTransfer.getData('projectId');
                        if (!projectId) return;
                        fetch(`/api/projects/${projectId}/folder`, {
                            method: 'PATCH',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ folderId: targetFolderId })
                        }).then(res => {
                            if (res.ok) { renderFolderTree(); renderProjectGallery(); }
                            else res.text().then(t => alert('이동 실패: ' + t));
                        });
                    });
                }

                // 전체 프로젝트 항목
                const allLi = document.createElement('li');
                allLi.className = 'folder-node' + (_currentFolderId === null ? ' selected-node' : '');
                const unfoldered = projects.filter(p => p.folderId == null).length;
                allLi.innerHTML = `<span>전체 프로젝트</span><span style="font-size:12px; color:var(--text-gray);">${unfoldered}</span>`;
                allLi.onclick = () => {
                    _currentFolderId = null;
                    _setFolderTitle('전체 프로젝트', null);
                    renderFolderTree();
                    renderProjectGallery();
                };
                _makeDrop(allLi, null);
                tree.appendChild(allLi);

                // 폴더별 항목
                folders.forEach(f => {
                    const count = projects.filter(p => p.folderId === f.folderId).length;
                    const li = document.createElement('li');
                    li.className = 'folder-node' + (_currentFolderId === f.folderId ? ' selected-node' : '');
                    li.innerHTML = `<span>📁 ${f.folderName}</span><span style="font-size:12px; color:var(--text-gray);">${count}</span>`;
                    li.onclick = () => {
                        _currentFolderId = f.folderId;
                        _setFolderTitle(f.folderName, f.folderId);
                        renderFolderTree();
                        renderProjectGallery();
                    };
                    _makeDrop(li, f.folderId);
                    tree.appendChild(li);
                });

                // 현재 선택된 폴더가 있으면 타이틀 갱신 (렌더 후에도 연필 아이콘 유지)
                if (_currentFolderId !== null) {
                    const current = folders.find(f => f.folderId === _currentFolderId);
                    if (current) _setFolderTitle(current.folderName, current.folderId);
                }
            }).catch(() => {});
        }

        // 백엔드에서 실제 프로젝트 데이터를 가져와 저장소에 그려주는 함수
        function renderProjectGallery() {
            const gallery = document.getElementById('project-archive-gallery-element');
            if (!gallery) return;

            gallery.innerHTML = '<div style="text-align:center; padding: 50px; grid-column: span 3; font-size: 14px; color: var(--text-gray);">불러오는 중...</div>';

            fetch('/api/projects/my')
                .then(response => {
                    if (!response.ok) throw new Error('인증 필요');
                    return response.json();
                })
                .then(allProjects => {
                    const projects = _currentFolderId === null
                        ? allProjects.filter(p => p.folderId == null)
                        : allProjects.filter(p => p.folderId === _currentFolderId);
                    gallery.innerHTML = '';

                    if (!projects || projects.length === 0) {
                        gallery.innerHTML = '<div style="text-align:center; padding: 50px; grid-column: span 3; color: var(--text-gray);">보관 내역이 비어있습니다.</div>';
                        return;
                    }

                    projects.forEach(project => {
                        const dateStr = project.createdAt ? project.createdAt.substring(0, 10) : '';
                        const item = document.createElement('div');
                        item.className = 'gallery-item';
                        item.draggable = true;
                        item.dataset.projectId = project.projectId;
                        item.addEventListener('dragstart', e => {
                            e.dataTransfer.setData('projectId', project.projectId);
                            item.style.opacity = '0.5';
                        });
                        item.addEventListener('dragend', () => { item.style.opacity = ''; });

                        // 프리뷰 영역
                        const preview = document.createElement('div');
                        preview.className = 'gallery-preview';
                        preview.style.cursor = 'pointer';
                        preview.onclick = () => window.location.href = '/dashboard?projectId=' + project.projectId;

                        // moodboardData에서 카테고리별 이미지 배열 전체 추출
                        let categoryThumbs = [];
                        if (project.moodboardData) {
                            try {
                                const parsed = JSON.parse(project.moodboardData);
                                parsed.forEach(cat => {
                                    const validAssets = (cat.assets || []).filter(u => u && u.startsWith('http'));
                                    if (validAssets.length > 0) {
                                        categoryThumbs.push({ category: cat.category, assets: validAssets });
                                    }
                                });
                            } catch(e) {}
                        }

                        if (categoryThumbs.length > 0) {
                            preview.style.padding = '0';
                            preview.style.border = 'none';
                            preview.style.overflow = 'hidden';
                            // 4칸 × 2줄 외부 그리드
                            const outerGrid = document.createElement('div');
                            outerGrid.style.cssText = 'display:grid; grid-template-columns:repeat(4,1fr); grid-template-rows:repeat(2,1fr); width:100%; height:100%; gap:2px;';

                            for (let i = 0; i < 8; i++) {
                                const cell = document.createElement('div');
                                cell.style.cssText = 'overflow:hidden; position:relative; background:var(--bg-card-inner);';

                                if (i < categoryThumbs.length) {
                                    const { category, assets } = categoryThumbs[i];
                                    const count = Math.min(assets.length, 4);

                                    // 기존 layout-N / thumb-N CSS 클래스로 카테고리 내 이미지 그리드
                                    const innerGrid = document.createElement('div');
                                    innerGrid.className = `layout-${count}`;
                                    innerGrid.style.cssText = 'display:grid; width:100%; height:100%; overflow:hidden; gap:1px;';

                                    assets.slice(0, 4).forEach((url, idx) => {
                                        const thumb = document.createElement('div');
                                        thumb.className = `thumb-${idx}`;
                                        thumb.style.overflow = 'hidden';
                                        const img = document.createElement('img');
                                        img.src = url;
                                        img.style.cssText = 'width:100%; height:100%; object-fit:cover; display:block;';
                                        thumb.appendChild(img);
                                        innerGrid.appendChild(thumb);
                                    });

                                    cell.appendChild(innerGrid);

                                    const label = document.createElement('div');
                                    label.style.cssText = 'position:absolute; bottom:0; left:0; right:0; background:rgba(0,0,0,0.55); color:#fff; font-size:8px; text-align:center; padding:2px 1px; line-height:1.3; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; z-index:1;';
                                    label.textContent = category;
                                    cell.appendChild(label);
                                }

                                outerGrid.appendChild(cell);
                            }
                            preview.appendChild(outerGrid);
                        } else {
                            const placeholder = document.createElement('span');
                            placeholder.style.cssText = 'color:var(--primary-blue); font-weight:bold;';
                            placeholder.textContent = '[ 무드보드 + 대시보드 열기 ]';
                            preview.appendChild(placeholder);
                        }

                        // 프로젝트명
                        const titleDiv = document.createElement('div');
                        titleDiv.style.cssText = 'font-weight:700; font-size:13px; margin-bottom:12px; color:var(--text-dark); line-height:1.4;';
                        titleDiv.textContent = project.projectName;

                        // 하단 날짜 + 삭제 버튼
                        const footer = document.createElement('div');
                        footer.style.cssText = 'display:flex; justify-content:space-between; align-items:center; border-top:1px solid var(--border-color); padding-top:10px;';

                        const dateSpan = document.createElement('span');
                        dateSpan.style.cssText = 'font-size:11px; color:var(--text-gray);';
                        dateSpan.textContent = dateStr;

                        const deleteBtn = document.createElement('button');
                        deleteBtn.className = 'btn-action';
                        deleteBtn.style.cssText = 'background-color:#B4B4B4; color:black; border:none; padding:5px 15px !important; font-size:13px; border-radius:4px; cursor:pointer;';
                        deleteBtn.textContent = '삭제';
                        deleteBtn.onclick = (e) => { e.stopPropagation(); deleteProjectFromApi(project.projectId); };

                        footer.appendChild(dateSpan);
                        footer.appendChild(deleteBtn);

                        item.appendChild(preview);
                        item.appendChild(titleDiv);
                        item.appendChild(footer);
                        gallery.appendChild(item);
                    });
                })
                .catch(() => {
                    gallery.innerHTML = '<div style="text-align:center; padding: 50px; grid-column: span 3; color: var(--text-gray);">프로젝트를 불러오지 못했습니다. 로그인 상태를 확인해주세요.</div>';
                });
        }

        // 프로젝트 개별 삭제 (실제 API 연동)
        function deleteProjectFromApi(projectId) {
            if (!confirm("선택한 프로젝트 기획 리포트를 영구 삭제하시겠습니까?")) return;
            fetch(`/api/projects/${projectId}`, { method: 'DELETE' })
                .then(res => {
                    if (!res.ok) return res.text().then(t => { throw new Error(t); });
                    renderProjectGallery();
                })
                .catch(err => alert('삭제 실패: ' + err.message));
        }


        // 페이지 로드 시 초기 화면 렌더링 (✨ 현재 주소창의 위치에 따라 내부 인덱스 동기화 설정)
        window.onload = function() {
            if (typeof renderFolderTree === "function") renderFolderTree();
            if (typeof renderProjectGallery === "function") renderProjectGallery();

            // 1. 소셜 로그인 감지 (기존 로직 유지)
            const urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('socialLogin') === 'true') {
                sessionStorage.setItem("isLoggedIn", "true");
                isLoggedIn = true;
                window.history.replaceState({}, document.title, window.location.pathname);
            }
            syncAuthUI();

            // 2. 경로별 스텝 설정 (매핑 테이블 활용)
            const path = window.location.pathname;
            const pathStepMap = {
                '/brandsync': 6,
                '/reference': 7,
                '/moodboard': 8,
                '/dashboard': 9
            };

            const currentStep = pathStepMap[path];

            if (currentStep) {
                currentIdx = currentStep;

                // 스텝바 활성화
                const stepBar = document.getElementById('global-step-bar');
                if (stepBar) stepBar.style.display = 'flex';

                // 해당 스텝(st-6, st-7 등) 활성화
                const activeStep = document.getElementById(`st-${currentStep}`);
                if (activeStep) activeStep.classList.add('active');

                // 페이지 뷰(page6, page7 등) 활성화 (존영하는 경우만)
                const activePage = document.getElementById(`page${currentStep}`);
                if (activePage) activePage.classList.add('active-view');
            }

            // 그 외 메인 경로 설정
            if (path === '/') currentIdx = 1;
            else if (path === '/login') currentIdx = 3;
            else if (path === '/signup') currentIdx = 4;
        };


        // 3. 페이지 네비게이션 및 제어

        // 로고 클릭 시 메인 페이지로 이동
        function handleLogoClick() { window.location.href = '/'; }

        // 워크스페이스 진입 시 로그인 여부 체크 (새 프로젝트 시작 → 이전 작업 세션 초기화)
        function handleEntrance() {
            const 찐로그인상태 = sessionStorage.getItem("isLoggedIn") === "true";
            if (찐로그인상태) {
                ['brandQ1','brandQ2','brandMoods','brandKeywords',
                 'aiMoodboardData','referenceSelections','finalMoodboardData','dashboardData'
                ].forEach(k => sessionStorage.removeItem(k));
                window.location.href = '/brandsync';
            } else {
                alert("로그인 후 이용할 수 있습니다.");
                window.location.href = '/login';
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
            if (idx === 4) { window.location.href = '/signup'; return; }
            if (idx === 6) { window.location.href = '/brandsync'; return; }
            if (idx === 7) { window.location.href = '/reference'; return; } // 🚀 7번 스텝 주소 추가 완료!
            if (idx === 8) { window.location.href = '/moodboard'; return; } // 🚀 8번(무드보드) 스텝 주소 추가 완료!
            if (idx === 9) { window.location.href = '/dashboard'; return; }

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

        // 스텝바 탭 클릭 시 단계 완료 여부 검증 후 이동
        function navigateToStep(idx) {
            const ALERT_MSG = "현재 진행중인 단계를 완료한 후에 다음 단계로 넘어갈 수 있습니다.";

            if (idx === 6) { navigateTo(6); return; }

            if (idx === 7) {
                if (!sessionStorage.getItem("brandQ1") ||
                    !sessionStorage.getItem("brandMoods") ||
                    !sessionStorage.getItem("brandKeywords")) {
                    alert(ALERT_MSG); return;
                }
                navigateTo(7); return;
            }

            if (idx === 8) {
                if (!sessionStorage.getItem("finalMoodboardData")) {
                    alert(ALERT_MSG); return;
                }
                navigateTo(8); return;
            }

            if (idx === 9) {
                if (!sessionStorage.getItem("dashboardData")) {
                    alert(ALERT_MSG); return;
                }
                navigateTo(9); return;
            }

            navigateTo(idx);
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

        // 이메일 중복 확인
        function checkEmailDuplicate() {
            const email = (document.getElementById('join-email')?.value || '').trim();
            if (!email) { alert("이메일을 입력해주세요."); return; }

            fetch(`/api/members/check-email?email=${encodeURIComponent(email)}`)
                .then(async res => {
                    const msg = await res.text();
                    alert(msg);
                })
                .catch(() => alert("서버와 통신 중 문제가 발생했습니다."));
        }

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

            if (pw.length < 8) {
                alert("⚠️ 비밀번호는 최소 8자 이상이어야 합니다.");
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
            const mypageMenu = document.getElementById('mypage-nav-menu');

            if (isLoggedIn) {
                if (loginMenu) loginMenu.style.display = "none";
                if (mypageMenu) mypageMenu.style.display = "list-item";
            } else {
                if (loginMenu) loginMenu.style.display = "list-item";
                if (mypageMenu) mypageMenu.style.display = "none";
            }
        }


        function executeModify() {
            const currentPw = (document.getElementById('modify-pw-current')?.value || '').trim();
            const newPw     = (document.getElementById('modify-pw')?.value || '').trim();
            const newPwConfirm = (document.getElementById('modify-pw-confirm')?.value || '').trim();

            if (!currentPw) { alert("현재 비밀번호를 입력해주세요."); return; }
            if (!newPw)     { alert("새로운 비밀번호를 입력해주세요."); return; }
            if (newPw.length < 8) { alert("새로운 비밀번호는 최소 8자 이상이어야 합니다."); return; }
            if (newPw !== newPwConfirm) { alert("새로운 비밀번호가 일치하지 않습니다."); return; }

            fetch('/api/members/me', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ currentPassword: currentPw, password: newPw })
            }).then(async res => {
                const msg = await res.text();
                if (res.ok) {
                    alert("비밀번호가 성공적으로 변경되었습니다.");
                    window.location.href = '/';
                } else {
                    alert(msg);
                }
            }).catch(() => alert("서버와 통신 중 문제가 발생했습니다."));
        }

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



        // 브랜드 기획 동기화 - Q1~Q4 모두 입력 후 다음 단계 이동 가능


        // 스타일 선택 및 4개 제한 로직 (✨ 선택 4개 초과 방지 조건 >= 4 로 엄격하게 수정)
        function handleSubCardSelection(cardElement, keywordValue) {
            if (cardElement.classList.contains('disabled-style')) return;

            if (cardElement.classList.contains('chosen-style')) {
                cardElement.classList.remove('chosen-style');
                selectedSubKeywordsList = selectedSubKeywordsList.filter(k => k !== keywordValue);
            } else {
                if (selectedSubKeywordsList.length >= 4) {
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

        // 🚀 [추가 완료] 무드보드 페이지에서 대시보드(9번)로 곧바로 이동하는 함수 정의
        function showLiveDashboard() {
            window.location.href = '/dashboard';
        }

        // 🚀 6. 프로젝트 저장 모달 제어 및 대시보드 백엔드 통신 저장 로직
        function openSaveProjectModal() {
            const modal = document.getElementById('custom-save-modal-overlay');
            if (modal) modal.style.display = 'flex';
        }

        function closeSaveProjectModal() {
            const modal = document.getElementById('custom-save-modal-overlay');
            if (modal) modal.style.display = 'none';
        }

        function showAfterSaveDialog() {
            const dialog = document.getElementById('after-save-dialog');
            if (dialog) dialog.style.display = 'flex';
        }

        // 🔥 DB NOT NULL 제약조건(BRAND_INTRO) 및 키워드 매핑을 완벽히 준수하는 DTO 매핑 저장 함수
        function executeAdvancedProjectSave() {
            const titleInput = document.getElementById('modal-project-title-input');
            const newFolderInput = document.getElementById('modal-new-folder-input');
            const folderSelect = document.getElementById('modal-existing-folder-select');

            const title = titleInput ? titleInput.value.trim() : "신규 생성 매칭 가이드 모델 셋";
            const newFolder = newFolderInput ? newFolderInput.value.trim() : "";
            const existingFolder = folderSelect ? folderSelect.value : "";

            if (!title) {
                alert("⚠️ 저장할 프로젝트명을 입력해주세요.");
                return;
            }

            // 세션 스토리지에 있는 기획 Q1 데이터(BRAND_INTRO 매핑용) 및 기타 데이터 수집
            const q1 = sessionStorage.getItem("brandQ1") || "브랜드 라인 요약 기본값";
            const q2 = sessionStorage.getItem("brandQ2") || "타겟 오브젝트 검색 기본값";

            // 키워드 ID 배열 가공
            const keywordIdsList = JSON.parse(sessionStorage.getItem("brandKeywords") || "[]");

            // 이미지 url 배열 추출
            const references = JSON.parse(sessionStorage.getItem("finalMoodboardData") || "[]");
            let imgUrlsList = [];
            references.forEach(cat => {
                if (cat.assets && Array.isArray(cat.assets)) {
                    imgUrlsList = imgUrlsList.concat(cat.assets);
                }
            });

            // 🚀 [폴더 ID 연동 추가]
            const folderId = (existingFolder && existingFolder !== "") ? parseInt(existingFolder) : null;

            // 카테고리 구조 포함한 무드보드 전체 데이터 (섹션6 복원용)
            const moodboardDataRaw = sessionStorage.getItem("finalMoodboardData") || "[]";

            // 대시보드 AI 분석 결과 추출 (재호출 방지용 — 이미 계산된 결과 재활용)
            const dashRaw = sessionStorage.getItem("dashboardData");
            let imageAlignmentsRaw = "[]";
            let brandProfileRaw = null;
            let analysisInsightRaw = null;
            let similarityScore = null;
            let positionX = null;
            let positionY = null;
            if (dashRaw) {
                try {
                    const d = JSON.parse(dashRaw);
                    imageAlignmentsRaw = JSON.stringify(d?.evidence?.imageAlignments || []);
                    if (d?.brandProfile) brandProfileRaw = JSON.stringify(d.brandProfile);
                    if (d?.insight) analysisInsightRaw = JSON.stringify(d.insight);
                    if (d?.consistency?.score != null) similarityScore = d.consistency.score;
                    if (d?.position?.x != null) positionX = d.position.x;
                    if (d?.position?.y != null) positionY = d.position.y;
                } catch(e) {}
            }

            // 백엔드 DTO(ProjectCreateRequest) 규격에 정확히 맞춘 객체 조립
            const payload = {
                projectName: title,
                brandIntro: q1,
                referenceType: q2,
                keywordIds: keywordIdsList,
                imgUrls: imgUrlsList,
                folderId: folderId,
                moodboardData: moodboardDataRaw,
                imageAlignmentsData: imageAlignmentsRaw,
                brandProfile: brandProfileRaw,
                analysisInsight: analysisInsightRaw,
                similarityScore: similarityScore,
                positionX: positionX,
                positionY: positionY
            };

            fetch('/api/projects/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            })
            .then(async response => {
                if (response.ok) {
                    closeSaveProjectModal();
                    showAfterSaveDialog();
                } else {
                    const err = await response.text();
                    alert(err);
                }
            })
            .catch(error => {
                console.error("통신 에러:", error);
                alert("🚨 서버 통신 중 오류가 발생했습니다.");
            });
        }

        // 현재 선택된 폴더 삭제
        function deleteSelectedFolderInSystem() {
            if (_currentFolderId === null) {
                alert("삭제할 폴더를 먼저 선택해주세요.");
                return;
            }
            if (!confirm("선택한 폴더를 삭제하시겠습니까?\n폴더 내 프로젝트는 전체 프로젝트로 이동됩니다.")) return;

            fetch(`/api/folders/${_currentFolderId}`, { method: 'DELETE' })
                .then(async res => {
                    if (res.ok) {
                        _currentFolderId = null;
                        const titleEl = document.getElementById('current-folder-title-display');
                        if (titleEl) titleEl.innerText = '전체 프로젝트';
                        renderFolderTree();
                        renderProjectGallery();
                    } else {
                        const err = await res.text();
                        alert("폴더 삭제 실패: " + err);
                    }
                })
                .catch(() => alert("서버와 통신 중 문제가 발생했습니다."));
        }

        // 🚀 새 폴더 생성을 위한 함수 정의
        function createNewFolderInSystem() {
            const folderName = prompt("새 폴더 이름을 입력하세요:");
            if (!folderName || folderName.trim() === "") {
                alert("폴더 이름을 입력해주세요.");
                return;
            }

            // 서버로 폴더 생성 요청
            fetch('/api/folders/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ folderName: folderName })
            })
            .then(async response => {
                if (response.ok) {
                    alert("폴더가 성공적으로 생성되었습니다!");
                    renderFolderTree();
                } else {
                    const err = await response.text();
                    alert("폴더 생성 실패: " + err);
                }
            })
            .catch(error => {
                console.error("폴더 생성 에러:", error);
                alert("서버와 통신 중 문제가 발생했습니다.");
            });
        }

        // 브랜드 대시보드 - 처음부터 다시 기획하기
                function resetAllPlanningData() {
                    // 1. 확인 메시지
                    if (!confirm("처음부터 다시 기획하시겠습니까? 프로젝트가 저장되지 않습니다.")) return;

                    // 2. 중요: 세션 데이터 모두 삭제
                    sessionStorage.removeItem("brandQ1");
                    sessionStorage.removeItem("brandQ2");
                    sessionStorage.removeItem("brandMoods");
                    sessionStorage.removeItem("brandKeywords");
                    sessionStorage.removeItem("finalMoodboardData");
                    sessionStorage.removeItem("aiMoodboardData");

                    // 3. 페이지 이동
                    window.location.href = '/brandsync';
                }