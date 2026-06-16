
        // 1. 대시보드 및 프로젝트 저장소 - 가상 데이터(지금은 화면 보여주기용이라 나중에 삭제)
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


        // 2. 무드보드 빌드 및 시각화 (대시보드와 공유)
        function compileMoodboardCanvasHTML(dataArray) {
        const canvas8 = document.getElementById('live-moodboard-canvas');
        const canvas9 = document.getElementById('dashboard-inline-moodboard-canvas');
        canvas8.innerHTML = "";
        canvas9.innerHTML = "";

        dataArray.forEach((item, index) => {
            const count = item.assets.length;
            // 개수에 따라 layout-1, layout-2, layout-3, layout-4 클래스 할당
            const layoutClass = count >= 4 ? 'layout-4' : `layout-${count}`;

            const assetsHtml = item.assets.map((asset, i) => `
                <div class="mood-tile-thumb thumb-${i}">
                    ${asset}
                </div>
            `).join('');

            const htmlTemplate = `
                <div class="mood-tile">
                    <div class="mood-tile-title">${index + 1}. ${item.category}</div>
                    <div class="assets-container ${layoutClass}">
                        ${assetsHtml}
                    </div>
                </div>
            `;

            canvas8.innerHTML += htmlTemplate;
            canvas9.innerHTML += htmlTemplate;
        });
    }

        // 3. 프로젝트 저장소 렌더링 로직
        // 좌측 폴더 리스트 생성
        function renderFolderTree() {
            const tree = document.getElementById('system-folder-tree-element'); tree.innerHTML = "";
            virtualFolders.forEach(f => {
                const fCount = f.id === "all" ? virtualProjects.length : virtualProjects.filter(p => p.folder === f.id).length;
                const li = document.createElement('li');
                li.className = `folder-node ${f.id === currentSelectedFolderId ? 'selected-node' : ''}`;
                li.innerHTML = `<span>📁 ${f.name}</span> <span style="font-size:11px; opacity:0.7;">(${fCount})</span>`;
                li.onclick = function() {
                    currentSelectedFolderId = f.id;
                    document.getElementById('current-folder-title-display').innerText = f.name;
                    renderFolderTree(); renderProjectGallery();
                };
                tree.appendChild(li);
            });
        }

        // 우측 프로젝트 갤러리 리스트 생성
        function renderProjectGallery() {
            const target = document.getElementById('project-archive-gallery-element');
            target.innerHTML = "";

            let filtered = currentSelectedFolderId === "all" ? virtualProjects : virtualProjects.filter(p => p.folder === currentSelectedFolderId);

            if(filtered.length === 0) {
                target.innerHTML = "<div style='grid-column: span 3; text-align:center; padding:40px; color:var(--text-gray); font-size:13px;'>보관 내역이 비어있습니다.</div>";
                return;
            }

            filtered.forEach(p => {
                const item = document.createElement('div');
                item.className = "gallery-item";
                item.innerHTML = `
                    <div class="gallery-preview" onclick="loadArchivedProjectToDashboard('${p.id}')">
                        <span style="color:var(--primary-blue); font-weight:bold; margin-bottom:5px;">[ 무드보드 + 대시보드 열기 ]</span>
                    </div>
                    <div style="font-weight:700; font-size:13px; margin-bottom:12px; color:var(--text-dark); line-height:1.4;">${p.title}</div>
                    <div style="display:flex; justify-content:space-between; align-items:center; border-top:1px solid var(--border-color); padding-top:10px;">
                        <span style="font-size:11px; color:var(--text-gray);">${p.date}</span>
                        <button class="btn-action" style="background-color: #B4B4B4; color: black; border: none; padding:5px 15px !important; font-size:13px; border-radius:4px; cursor:pointer;" onclick="deleteProjectNode('${p.id}')">삭제</button>
                    </div>
                `;
                target.appendChild(item);
            });
        }


            // 4. 대시보드 UI 동기화 및 상세 가능
            // 프로젝트 저장소에서 프로젝트 클릭 시 해당 브랜드 대시보드 화면 띄워줌
            function updateDashboardUI(projectData) {
            console.log("대시보드 UI 동기화 중...", projectData);

            // (1) 헤더 제목 변경
            const dashHeader = document.querySelector('.dash-header-title h2');
            if (dashHeader) dashHeader.innerText = projectData.title;

            // (2) 브랜드 방향 키워드 렌더링
            const keywordContainer = document.getElementById('brand-keyword-container');
            if (keywordContainer) {
                // 저장된 키워드가 있으면 그것을 쓰고, 없으면 현재 고르고 있는 리스트(selectedSubKeywordsList)를 씁니다.
                const keywords = projectData.selectedKeywords || selectedSubKeywordsList;

                if (keywords && keywords.length > 0) {
                    keywordContainer.innerHTML = keywords.map(k => `
                        <span style="border:1px solid var(--primary-orange); color:var(--primary-orange); background:var(--selected-bg); padding:6px 14px; border-radius:20px; font-size:12px; font-weight:600; margin-right: 8px;">
                    ${k}
                </span>
                    `).join('');
                } else {
                    keywordContainer.innerHTML = '<span style="color:var(--text-gray); font-size:12px;">선택된 키워드가 없습니다.</span>';
                }
            }

            // (3) 벤치마크 데이터 업데이트
            const benchmarkList = document.getElementById('benchmark-list');
            if (benchmarkList && projectData.analysis && projectData.analysis.benchmarks) {
                benchmarkList.innerHTML = projectData.analysis.benchmarks.map(b => `
                    <div style="background:var(--bg-card-inner); border:1px solid var(--border-color); padding:10px 14px; border-radius:6px; display:flex; justify-content:space-between; font-size:13px; margin-bottom: 8px;">
                <span><strong>${b.name}</strong></span>
                <span style="color:var(--primary-orange); font-weight:700;">${b.score}%</span>
            </div>
                `).join('');
            }
        }

          // 기획 단계에서 선택한 키워드를 대시보드로 넘겨주고 화면을 전환하는 함수
                function showLiveDashboard() {
                    console.log("대시보드 확인 버튼 클릭됨! 키워드 전송 시작.");

                    // 1. 현재 기획 페이지에서 선택된 키워드들을 담은 데이터 객체 생성
                    const liveData = {
                        title: "기획 중인 브랜드",
                        selectedKeywords: selectedSubKeywordsList,
                        analysis: {
                            benchmarks: [{ name: "Brand-Sync Default", score: 87 }] // 기본 데이터
                        }
                    };

                    // 2. 대시보드 UI를 현재 기획 데이터로 업데이트
                    updateDashboardUI(liveData);

                    // 3. 페이지 이동
                    navigateTo(9);
                    console.log("대시보드로 이동 완료.");
                }


      // 현재 화면(대시보드)을 PDF로 변환 및 다운로드
      function exportToPDF() {
        const element = document.getElementById('page9');

        // 브랜드 대시보드 - PDF 저장 로직
        element.classList.add('pdf-export-mode');

        // 0.5초 기다려서 CSS가 먼저 적용되게 함
        setTimeout(() => {
            const opt = {
                margin:       10,
                filename:     'Brand-Sync_Report.pdf',
                image:        { type: 'jpeg', quality: 1 },
                html2canvas:  {
                    scale: 2,
                    useCORS: true,
                    logging: false,
                    width: element.clientWidth,
                    windowWidth: element.clientWidth
                },
                jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
            };

            html2pdf().set(opt).from(element).save().then(() => {
                // 변환 완료 후 원래 스타일로 복구
                element.classList.remove('pdf-export-mode');
            });
        }, 500);
    }


        // 저장 모달창 표시 및 폴더 목록 갱신
        function openSaveProjectModal() {
            const overlay = document.getElementById('custom-save-modal-overlay');
            const selectBox = document.getElementById('modal-existing-folder-select');
            selectBox.innerHTML = "";
            virtualFolders.forEach(f => {
                if(f.id !== "all") selectBox.innerHTML += `<option value="${f.id}">${f.name}</option>`;
            });
            overlay.style.display = "flex";
        }

        // 저장 모달창 닫기
        function closeSaveProjectModal() {
            document.getElementById('custom-save-modal-overlay').style.display = "none";
            document.getElementById('modal-new-folder-input').value = "";
        }

        // 프로젝트 데이터 저장 및 폴더 생성 처리
        function executeAdvancedProjectSave() {
            const projTitle = document.getElementById('modal-project-title-input').value.trim();
            const existingFolderId = document.getElementById('modal-existing-folder-select').value;
            const newFolderName = document.getElementById('modal-new-folder-input').value.trim();

            if(!projTitle) { alert("프로젝트 가이드북의 이름을 인풋하십시오."); return; }
            let targetFolderId = existingFolderId;

            if(newFolderName !== "") {
                const isFolderExists = virtualFolders.find(f => f.name === newFolderName);
                if(!isFolderExists) {
                    targetFolderId = "custom_gen_" + Date.now();
                    virtualFolders.push({ id: targetFolderId, name: newFolderName, isSystem: false });
                } else { targetFolderId = isFolderExists.id; }
            }

            virtualProjects.push({
                id: "p_user_built_" + Date.now(),
                folder: targetFolderId,
                title: projTitle,
                date: new Date().toISOString().substring(0,10),
                moodboardData: organizedDataGlobal,
                assets: [...currentLiveSessionAssets]
            });

            renderFolderTree(); renderProjectGallery(); closeSaveProjectModal();
            if(confirm("성공적으로 저장되었습니다. 프로젝트 저장소로 즉시 이동하시겠습니까?")) navigateTo(10); else navigateTo(4);
        }



        // 프로젝트 삭제
        function deleteProjectNode(projId) {
            event.stopPropagation();
            if(confirm("선택한 프로젝트 기획 리포트를 영구 삭제하시겠습니까?")) {
                virtualProjects = virtualProjects.filter(p => p.id !== projId);
                renderFolderTree(); renderProjectGallery();
            }
        }

          // 저장된 프로젝트 불러와서 대시보드 복원
          function loadArchivedProjectToDashboard(projId) {
              console.log("1. 클릭된 프로젝트 ID 확인:", projId); // 어디를 클릭했는지 확인

              const targetProj = virtualProjects.find(p => p.id === projId);

              if(!targetProj) {
                  console.error("2. 에러 발생: virtualProjects에서 해당 ID를 찾지 못했습니다.");
                  alert("프로젝트 정보를 찾을 수 없습니다.");
                  return;
              }

              console.log("3. 프로젝트 데이터를 찾았습니다:", targetProj.title);

              // 1. 선택한 프로젝트 데이터를 현재 세션 데이터로 복사
              currentLiveSessionAssets = [...targetProj.assets];
              console.log("4. 세션 데이터 복사 완료");

              // 2. 무드보드 렌더링
              compileMoodboardCanvasHTML(targetProj.moodboardData);
              console.log("5. 무드보드 렌더링 완료");

              // 3. 대시보드 UI를 해당 프로젝트 데이터로 동기화
              updateDashboardUI(targetProj);
              console.log("6. 대시보드 UI 동기화 완료");

              // 4. 이동
              console.log("7. 대시보드 페이지(9)로 이동 시도");
              navigateTo(9);
          }

            // 시스템 초기화
            window.addEventListener('load', () => {
                renderFolderTree();
                renderProjectGallery();
            });


          // 새 폴더 추가
          function createNewFolderInSystem() {
              const fName = prompt("새로운 보관 폴더 이름을 지정하십시오:");
              if(!fName || fName.trim() === "") return;
              virtualFolders.push({ id: "custom_" + Date.now(), name: fName.trim(), isSystem: false });
              renderFolderTree();
          }

          // 선택된 폴더 삭제
          function deleteSelectedFolderInSystem() {
              const currentFolder = virtualFolders.find(f => f.id === currentSelectedFolderId);
              if(!currentFolder || currentFolder.isSystem) {
                  alert("🔒 기본 루트 아카이브 폴더는 시스템 수렴 보존을 위해 삭제할 수 없습니다.");
                  return;
              }
              if(confirm(`⚠️ [${currentFolder.name}] 폴더를 삭제하시겠습니까? 내부 가이드북 데이터가 동시 파기됩니다.`)) {
                  virtualProjects = virtualProjects.filter(p => p.folder !== currentSelectedFolderId);
                  virtualFolders = virtualFolders.filter(f => f.id !== currentSelectedFolderId);
                  currentSelectedFolderId = "all";
                  document.getElementById('current-folder-title-display').innerText = "전체 프로젝트";
                  renderFolderTree();
                  renderProjectGallery();
              }
          }