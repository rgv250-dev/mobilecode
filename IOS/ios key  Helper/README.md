Keychain Helper 솔직히 이게 맞나? 자신이 점점 없어진다.


```

import Foundation

/// 나는 토큰으로만 사용중 
struct KeychainUtils {

    struct Credentials {
        var userName: String
        var password: String
    }

    private static let keychainQueue = DispatchQueue(label: "com.projectTest.keychainQueue")

    static func addKeyData(accountValue: String, passKeyValue: String) {
        keychainQueue.sync {
            guard let passwordData = passKeyValue.data(using: .utf8) else {
                print("Failed to convert password to Data")
                return
            }
            
            let query = keychainQuery(account: accountValue)
            var addQuery = query
            addQuery[kSecValueData] = passwordData
            addQuery[kSecAttrAccessible] = kSecAttrAccessibleWhenUnlocked

            let status = SecItemAdd(addQuery as CFDictionary, nil)
            if status == errSecDuplicateItem {
                updateItemOnKeyChain(value: passwordData, key: accountValue)
            } else if status != errSecSuccess {
                logKeychainError(status, operation: "Add Key")
            }
        }
    }

    static func updateItemOnKeyChain(value: Data, key: String) {
        keychainQueue.sync {
            let query = keychainQuery(account: key)
            let updateQuery: [CFString: Any] = [kSecValueData: value]

            let status = SecItemUpdate(query as CFDictionary, updateQuery as CFDictionary)
            if status == errSecSuccess {
                print("Update complete")
            } else {
                logKeychainError(status, operation: "Update Key")
            }
        }
    }

    // MARK: - Read Key Data
    static func readItemsOnKeyChain(accountData: String) -> String? {
        return keychainQueue.sync {
            var item: CFTypeRef?
            var query = keychainQuery(account: accountData)
            query[kSecReturnAttributes] = true
            query[kSecReturnData] = true

            let status = SecItemCopyMatching(query as CFDictionary, &item)
            guard status == errSecSuccess,
                  let existingItem = item as? [String: Any],
                  let data = existingItem[kSecValueData as String] as? Data,
                  let password = String(data: data, encoding: .utf8) else {
                logKeychainError(status, operation: "Read Key")
                return nil
            }

            return password
        }
    }

   
    static func deleteItemOnKeyChain(key: String) {
        keychainQueue.sync {
            let query = keychainQuery(account: key)
            let status = SecItemDelete(query as CFDictionary)
            if status == errSecSuccess {
                print("Remove key-data complete")
            } else {
                logKeychainError(status, operation: "Delete Key")
            }
        }
    }

    
    private static func keychainQuery(account: String) -> [CFString: Any] {
        return [
            kSecClass: kSecClassGenericPassword,
            kSecAttrAccount: account
        ]
    }

    private static func logKeychainError(_ status: OSStatus, operation: String) {
        if let errorMessage = SecCopyErrorMessageString(status, nil) {
            print("\(operation) failed: \(errorMessage)")
        } else {
            print("\(operation) failed with status code: \(status)")
        }
    }
}



```