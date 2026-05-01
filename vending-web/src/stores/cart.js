import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref(JSON.parse(localStorage.getItem('cartItems') || '[]'))
  const cabinetId = ref(JSON.parse(localStorage.getItem('cartCabinetId') || 'null'))
  const cabinetName = ref(localStorage.getItem('cartCabinetName') || '')

  const totalCount = computed(() =>
    items.value.reduce((sum, item) => sum + item.quantity, 0)
  )

  const totalPrice = computed(() =>
    items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  function addToCart(product, cabinetId, cabinetName) {
    if (cabinetId.value && cabinetId.value !== cabinetId) {
      return false
    }
    const existing = items.value.find(item => item.productId === product.productId)
    if (existing) {
      if (existing.quantity >= existing.stock) return false
      existing.quantity++
    } else {
      items.value.push({
        productId: product.productId,
        name: product.name,
        price: product.price,
        imageUrl: product.imageUrl,
        spec: product.spec,
        quantity: 1,
        stock: product.stock || 99
      })
    }
    cabinetId.value = cabinetId
    cabinetName.value = cabinetName
    saveCart()
    return true
  }

  function removeFromCart(productId) {
    const index = items.value.findIndex(item => item.productId === productId)
    if (index > -1) {
      items.value.splice(index, 1)
      saveCart()
    }
  }

  function updateQuantity(productId, quantity) {
    const item = items.value.find(item => item.productId === productId)
    if (item) {
      if (quantity <= 0) {
        removeFromCart(productId)
      } else {
        item.quantity = Math.min(quantity, item.stock)
        saveCart()
      }
    }
  }

  function clearCart() {
    items.value = []
    cabinetId.value = null
    cabinetName.value = ''
    localStorage.removeItem('cartItems')
    localStorage.removeItem('cartCabinetId')
    localStorage.removeItem('cartCabinetName')
  }

  function saveCart() {
    localStorage.setItem('cartItems', JSON.stringify(items.value))
    localStorage.setItem('cartCabinetId', JSON.stringify(cabinetId.value))
    localStorage.setItem('cartCabinetName', cabinetName.value)
  }

  return {
    items, cabinetId, cabinetName, totalCount, totalPrice,
    addToCart, removeFromCart, updateQuantity, clearCart
  }
})