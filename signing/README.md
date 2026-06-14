# Release 签名（本地）

通用脚本在 **`~/tools/scripts/`**：

```bash
~/tools/scripts/generate-release-keystore.sh "$(pwd)"
~/tools/scripts/setup-github-secrets.sh --project-dir "$(pwd)"
~/tools/scripts/setup-shared-secrets.sh
~/tools/scripts/setup-app-build-secrets.sh --project-dir "$(pwd)"
```

构建密钥 manifest：`signing/ci-build-secrets.manifest`（已提交，不含真实值）。

详细说明：`~/tools/scripts/app-build-secrets.README`
